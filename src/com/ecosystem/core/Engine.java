package com.ecosystem.core;

import com.ecosystem.config.SimulationConfig;
import com.ecosystem.events.EcosystemEvent;
import com.ecosystem.events.EcosystemEventBus;
import com.ecosystem.events.EcosystemEventType;
import com.ecosystem.exceptions.EntityOutOfBoundsException;
import com.ecosystem.grid.Cell;
import com.ecosystem.grid.SpatialGrid;
import com.ecosystem.model.Entity;
import com.ecosystem.model.Organism;
import com.ecosystem.model.decorator.AdrenalineBuff;
import com.ecosystem.model.decorator.CamouflageBuff;
import com.ecosystem.model.decorator.InfectedDebuff;
import com.ecosystem.model.decorator.OrganismDecorator;
import com.ecosystem.model.entities.*;
import com.ecosystem.util.EcosystemCollectors;
import com.ecosystem.util.EcosystemSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Main simulation coordinator driving time steps, entity updates, parallel metrics computation,
 * and environmental disaster events.
 */
public final class Engine {
    private static final Random RNG = new Random();

    private final SimulationConfig config;
    private final SpatialGrid<Entity> grid;
    private boolean running;
    private double speedMultiplier;
    private long totalTicks;
    private double totalSimulatedTime;

    // Statistics history for live graphing
    private final List<Integer> plantHistory;
    private final List<Integer> herbivoreHistory;
    private final List<Integer> carnivoreHistory;
    private final List<Integer> apexHistory;
    private final List<Double> diversityHistory;
    private int highestGeneration;
    private EcosystemSummary latestSummary;

    public Engine(SimulationConfig config) {
        this.config = config;
        this.grid = new SpatialGrid<>(config.worldWidth(), config.worldHeight(), config.cellSize());
        this.running = true;
        this.speedMultiplier = 1.0;
        this.totalTicks = 0;
        this.totalSimulatedTime = 0.0;

        this.plantHistory = new ArrayList<>();
        this.herbivoreHistory = new ArrayList<>();
        this.carnivoreHistory = new ArrayList<>();
        this.apexHistory = new ArrayList<>();
        this.diversityHistory = new ArrayList<>();
        this.highestGeneration = 1;

        populateWorld();
    }

    public void populateWorld() {
        for (int i = 0; i < config.initialPlants(); i++) {
            safeAddEntity(Plant.createDefault(findRandomValidPosition()));
        }
        for (int i = 0; i < config.initialHerbivores(); i++) {
            safeAddEntity(Herbivore.createDefault(findRandomValidPosition()));
        }
        for (int i = 0; i < config.initialCarnivores(); i++) {
            safeAddEntity(Carnivore.createDefault(findRandomValidPosition()));
        }
        for (int i = 0; i < config.initialApexPredators(); i++) {
            safeAddEntity(ApexPredator.createDefault(findRandomValidPosition()));
        }
        for (int i = 0; i < config.initialDecomposers(); i++) {
            safeAddEntity(Decomposer.createDefault(findRandomValidPosition()));
        }

        grid.synchronizeEntities();
        recordStatistics();
    }

    private void safeAddEntity(Entity entity) {
        if (entity == null) return;
        try {
            grid.addEntity(entity);
        } catch (EntityOutOfBoundsException e) {
            System.err.println("Skipped out-of-bounds spawn: " + e.getMessage());
        }
    }

    private Vector2D findRandomValidPosition() {
        for (int attempt = 0; attempt < 100; attempt++) {
            double x = 30.0 + RNG.nextDouble() * (config.worldWidth() - 60.0);
            double y = 30.0 + RNG.nextDouble() * (config.worldHeight() - 60.0);
            Vector2D candidate = new Vector2D(x, y);
            Cell cell = grid.getCellAtWorldCoords(candidate);
            if (cell != null && cell.getBiome().isTraversableByLand()) {
                return candidate;
            }
        }
        return new Vector2D(config.worldWidth() / 2.0, config.worldHeight() / 2.0);
    }

    /**
     * Advances simulation by deltaSeconds when running.
     */
    public synchronized void update(double deltaSeconds) {
        if (!running) return;
        executeStep(deltaSeconds);
    }

    /**
     * Advances simulation by exactly one step regardless of paused state (used by Step button).
     */
    public synchronized void stepOnce(double deltaSeconds) {
        executeStep(deltaSeconds);
        recordStatistics();
    }

    private void executeStep(double deltaSeconds) {
        double effectiveDelta = deltaSeconds * speedMultiplier;
        totalSimulatedTime += effectiveDelta;
        totalTicks++;
        EcosystemEventBus.getInstance().setCurrentTick(totalTicks);

        // Natural environmental regeneration
        grid.updateEnvironment(config.soilRegenRate() * effectiveDelta);

        // Snapshot of current entities prevents ConcurrentModificationException
        List<Entity> currentEntities = grid.getAllEntitiesSnapshot();
        for (Entity entity : currentEntities) {
            if (entity.isAlive()) {
                try {
                    entity.update(grid, effectiveDelta);
                } catch (EntityOutOfBoundsException e) {
                    Vector2D safePos = entity.getPosition().clamp(10, 10, config.worldWidth() - 10, config.worldHeight() - 10);
                    entity.setPosition(safePos);
                }

                // Unwrap expired temporary Adrenaline decorators
                if (entity instanceof AdrenalineBuff adrenaline && adrenaline.isExpired()) {
                    try {
                        grid.replaceEntity(adrenaline, adrenaline.getWrapped());
                    } catch (EntityOutOfBoundsException e) {
                        System.err.println("Failed to unwrap adrenaline buff: " + e.getMessage());
                    }
                }

                if (entity instanceof Organism org) {
                    if (org.getGeneration() > highestGeneration) {
                        highestGeneration = org.getGeneration();
                    }
                }
            }
        }

        // Natural seed-bank germination to sustain Lotka-Volterra rebound cycles
        if (totalTicks % 15 == 0) {
            int plantCount = grid.queryEntities(Plant.class).size();
            if (plantCount < 70) {
                Vector2D seedPos = findRandomValidPosition();
                Cell cell = grid.getCellAtWorldCoords(seedPos);
                if (cell != null && cell.getSoilNutrients().getCurrentAmount() > 15.0) {
                    safeAddEntity(Plant.createDefault(seedPos));
                }
            }
        }

        // Synchronize births, deaths, and decorator swaps
        grid.synchronizeEntities();

        // Record statistics and compute parallel metrics every 30 ticks
        if (totalTicks % 30 == 0) {
            recordStatistics();
            checkEmergentDisasters();
        }
    }

    /**
     * Thread-safe immediate entity spawning from interactive UI brush.
     */
    public synchronized void spawnEntity(Entity entity) throws EntityOutOfBoundsException {
        grid.addEntity(entity);
        grid.synchronizeEntities();
        this.latestSummary = computeParallelEcosystemMetrics();
    }

    /**
     * Sophisticated Parallel Stream calculation utilizing custom Collector.
     * Guaranteed thread-safe by operating on an isolated snapshot with pure associative combiner.
     */
    public EcosystemSummary computeParallelEcosystemMetrics() {
        List<Entity> snapshot = grid.getAllEntitiesSnapshot();
        return snapshot.parallelStream()
                .filter(Entity::isAlive)
                .collect(EcosystemCollectors.toEcosystemSummary());
    }

    private void recordStatistics() {
        this.latestSummary = computeParallelEcosystemMetrics();

        int plants = grid.queryEntities(Plant.class).size();
        int herbivores = grid.queryEntities(Herbivore.class).size();
        int carnivores = grid.queryEntities(Carnivore.class).size();
        int apex = grid.queryEntities(ApexPredator.class).size();

        plantHistory.add(plants);
        herbivoreHistory.add(herbivores);
        carnivoreHistory.add(carnivores);
        apexHistory.add(apex);
        diversityHistory.add(latestSummary.shannonDiversityIndex());

        if (plantHistory.size() > 200) {
            plantHistory.remove(0);
            herbivoreHistory.remove(0);
            carnivoreHistory.remove(0);
            apexHistory.remove(0);
            diversityHistory.remove(0);
        }
    }

    /**
     * Emergent environmental disaster triggers based on population thresholds.
     */
    private void checkEmergentDisasters() {
        int plants = grid.queryEntities(Plant.class).size();
        int herbivores = grid.queryEntities(Herbivore.class).size();

        if (plants > 180 && RNG.nextDouble() < 0.15) {
            triggerWildfire(findRandomValidPosition(), 120.0);
        }

        if (herbivores > 100 && RNG.nextDouble() < 0.10) {
            triggerDrought(0.5);
        }
    }

    public synchronized void triggerWildfire(Vector2D origin, double radius) {
        Vector2D fireCenter = origin;
        List<Plant> caughtInFire = grid.queryEntitiesNear(fireCenter, radius, Plant.class);
        if (caughtInFire.isEmpty()) {
            List<Plant> allPlants = grid.queryEntities(Plant.class);
            if (!allPlants.isEmpty()) {
                fireCenter = allPlants.get(RNG.nextInt(allPlants.size())).getPosition();
                caughtInFire = grid.queryEntitiesNear(fireCenter, radius, Plant.class);
            }
        }
        for (Plant plant : caughtInFire) {
            plant.markDead();
            Cell cell = grid.getCellAtWorldCoords(plant.getPosition());
            if (cell != null) {
                cell.getSoilNutrients().replenish(25.0);
            }
        }
        grid.synchronizeEntities();
        this.latestSummary = computeParallelEcosystemMetrics();
        EcosystemEventBus.getInstance().publish(EcosystemEvent.of(
                EcosystemEventType.ENVIRONMENTAL_DISASTER,
                "Wildfire burned " + caughtInFire.size() + " plants",
                fireCenter,
                totalTicks,
                null
        ));
    }

    public synchronized void triggerDrought(double severity) {
        for (int x = 0; x < grid.getCols(); x++) {
            for (int y = 0; y < grid.getRows(); y++) {
                Cell cell = grid.getCellAtGridCoords(x, y);
                if (cell != null) {
                    try {
                        if (cell.getSoilNutrients().hasAvailable()) {
                            cell.getSoilNutrients().consume(cell.getSoilNutrients().getCurrentAmount() * severity);
                        }
                        if (cell.getMoisture().hasAvailable()) {
                            cell.getMoisture().consume(cell.getMoisture().getCurrentAmount() * severity);
                        }
                    } catch (Exception e) {
                        System.err.println("Drought resource adjustment warning: " + e.getMessage());
                    }
                }
            }
        }
        // Inflict mild dehydration stress on plants so drought has immediate trophic impact
        for (Plant plant : grid.queryEntities(Plant.class)) {
            plant.consumeEnergy(plant.getEnergy() * (severity * 0.25));
        }
        grid.synchronizeEntities();
        this.latestSummary = computeParallelEcosystemMetrics();
        EcosystemEventBus.getInstance().publish(EcosystemEvent.of(
                EcosystemEventType.ENVIRONMENTAL_DISASTER,
                "Severe Drought depleted soil & moisture",
                new Vector2D(config.worldWidth() / 2, config.worldHeight() / 2),
                totalTicks,
                null
        ));
    }

    public synchronized void triggerFertileBloom() {
        for (int x = 0; x < grid.getCols(); x++) {
            for (int y = 0; y < grid.getRows(); y++) {
                Cell cell = grid.getCellAtGridCoords(x, y);
                if (cell != null && cell.getBiome().isTraversableByLand()) {
                    cell.getSoilNutrients().replenish(40.0);
                    cell.getMoisture().replenish(30.0);
                }
            }
        }
        for (int i = 0; i < 25; i++) {
            safeAddEntity(Plant.createDefault(findRandomValidPosition()));
        }
        grid.synchronizeEntities();
        this.latestSummary = computeParallelEcosystemMetrics();
        EcosystemEventBus.getInstance().publish(EcosystemEvent.of(
                EcosystemEventType.FERTILE_BLOOM,
                "Monsoon Bloom spawned 25 plants",
                new Vector2D(config.worldWidth() / 2, config.worldHeight() / 2),
                totalTicks,
                null
        ));
    }

    /**
     * Dynamically wraps a healthy organism with the InfectedDebuff decorator.
     */
    public synchronized boolean infectRandomOrganism() {
        List<Organism> candidates = grid.queryEntities(Organism.class).stream()
                .filter(o -> !OrganismDecorator.hasDecorator(o, InfectedDebuff.class))
                .filter(o -> o instanceof Animal || (o instanceof OrganismDecorator dec && dec.unwrap() instanceof Animal))
                .toList();

        if (candidates.isEmpty()) {
            candidates = grid.queryEntities(Organism.class).stream()
                    .filter(o -> !OrganismDecorator.hasDecorator(o, InfectedDebuff.class))
                    .toList();
        }
        if (candidates.isEmpty()) return false;

        Organism target = candidates.get(RNG.nextInt(candidates.size()));
        try {
            grid.replaceEntity(target, new InfectedDebuff(target, 2.5));
            grid.synchronizeEntities();
            EcosystemEventBus.getInstance().publish(EcosystemEvent.of(
                    EcosystemEventType.TROPHIC_COLLAPSE_WARNING,
                    "Pathogen outbreak infected " + target.getSpeciesName(),
                    target.getPosition(),
                    totalTicks,
                    target
            ));
            return true;
        } catch (EntityOutOfBoundsException e) {
            return false;
        }
    }

    /**
     * Dynamically wraps a random Herbivore or Animal with CamouflageBuff or AdrenalineBuff.
     */
    public synchronized boolean buffRandomOrganism() {
        List<Organism> candidates = grid.queryEntities(Organism.class).stream()
                .filter(o -> o instanceof Animal || (o instanceof OrganismDecorator dec && dec.unwrap() instanceof Animal))
                .filter(o -> !OrganismDecorator.hasDecorator(o, CamouflageBuff.class))
                .toList();

        if (candidates.isEmpty()) return false;
        Organism target = candidates.get(RNG.nextInt(candidates.size()));
        try {
            Organism decorated = (target instanceof Herbivore || (target instanceof OrganismDecorator dec && dec.unwrap() instanceof Herbivore))
                    ? new CamouflageBuff(target, 0.5)
                    : new AdrenalineBuff(target, 6.0);
            String buffName = (decorated instanceof CamouflageBuff) ? "Camouflage" : "Adrenaline";
            grid.replaceEntity(target, decorated);
            grid.synchronizeEntities();
            EcosystemEventBus.getInstance().publish(EcosystemEvent.of(
                    EcosystemEventType.SPECIES_DOMINANCE,
                    buffName + " trait applied to " + target.getSpeciesName(),
                    target.getPosition(),
                    totalTicks,
                    decorated
            ));
            return true;
        } catch (EntityOutOfBoundsException e) {
            return false;
        }
    }

    public SpatialGrid<Entity> getGrid() {
        return grid;
    }

    public SimulationConfig getConfig() {
        return config;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public void toggleRunning() {
        this.running = !this.running;
    }

    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    public void setSpeedMultiplier(double speedMultiplier) {
        this.speedMultiplier = Math.max(0.1, Math.min(10.0, speedMultiplier));
    }

    public long getTotalTicks() {
        return totalTicks;
    }

    public double getTotalSimulatedTime() {
        return totalSimulatedTime;
    }

    public int getHighestGeneration() {
        return highestGeneration;
    }

    public List<Integer> getPlantHistory() {
        return plantHistory;
    }

    public List<Integer> getHerbivoreHistory() {
        return herbivoreHistory;
    }

    public List<Integer> getCarnivoreHistory() {
        return carnivoreHistory;
    }

    public List<Integer> getApexHistory() {
        return apexHistory;
    }

    public List<Double> getDiversityHistory() {
        return diversityHistory;
    }

    public EcosystemSummary getLatestSummary() {
        return latestSummary;
    }

    public synchronized void reset() {
        this.running = false;
        List<Entity> all = grid.getAllEntitiesSnapshot();
        for (Entity e : all) {
            e.markDead();
        }
        grid.synchronizeEntities();
        plantHistory.clear();
        herbivoreHistory.clear();
        carnivoreHistory.clear();
        apexHistory.clear();
        diversityHistory.clear();
        highestGeneration = 1;
        totalTicks = 0;
        totalSimulatedTime = 0.0;
        EcosystemEventBus.getInstance().setCurrentTick(0);
        populateWorld();
        this.running = true;
    }
}
