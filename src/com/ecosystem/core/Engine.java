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
public class Engine {
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
            Vector2D pos = findRandomValidPosition();
            if (pos != null) {
                try {
                    grid.addEntity(Plant.createDefault(pos));
                } catch (EntityOutOfBoundsException ignored) {}
            }
        }

        for (int i = 0; i < config.initialHerbivores(); i++) {
            Vector2D pos = findRandomValidPosition();
            if (pos != null) {
                try {
                    grid.addEntity(Herbivore.createDefault(pos));
                } catch (EntityOutOfBoundsException ignored) {}
            }
        }

        for (int i = 0; i < config.initialCarnivores(); i++) {
            Vector2D pos = findRandomValidPosition();
            if (pos != null) {
                try {
                    grid.addEntity(Carnivore.createDefault(pos));
                } catch (EntityOutOfBoundsException ignored) {}
            }
        }

        for (int i = 0; i < config.initialApexPredators(); i++) {
            Vector2D pos = findRandomValidPosition();
            if (pos != null) {
                try {
                    grid.addEntity(ApexPredator.createDefault(pos));
                } catch (EntityOutOfBoundsException ignored) {}
            }
        }

        for (int i = 0; i < config.initialDecomposers(); i++) {
            Vector2D pos = findRandomValidPosition();
            if (pos != null) {
                try {
                    grid.addEntity(Decomposer.createDefault(pos));
                } catch (EntityOutOfBoundsException ignored) {}
            }
        }

        grid.synchronizeEntities();
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
     * Advances simulation by deltaSeconds.
     */
    public synchronized void update(double deltaSeconds) {
        if (!running) return;

        double effectiveDelta = deltaSeconds * speedMultiplier;
        totalSimulatedTime += effectiveDelta;
        totalTicks++;

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

                if (entity instanceof Organism org) {
                    if (org.getGeneration() > highestGeneration) {
                        highestGeneration = org.getGeneration();
                    }
                }
            }
        }

        // Synchronize births and deaths
        grid.synchronizeEntities();

        // Record statistics and compute parallel metrics every 30 ticks
        if (totalTicks % 30 == 0) {
            recordStatistics();
            checkEmergentDisasters();
        }
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

        // Overcrowded plants can ignite a Wildfire
        if (plants > 180 && RNG.nextDouble() < 0.15) {
            triggerWildfire(findRandomValidPosition(), 120.0);
        }

        // Severe herbivore overgrazing triggers Drought
        if (herbivores > 100 && RNG.nextDouble() < 0.10) {
            triggerDrought(0.5);
        }
    }

    public void triggerWildfire(Vector2D origin, double radius) {
        List<Plant> caughtInFire = grid.queryEntitiesNear(origin, radius, Plant.class);
        for (Plant plant : caughtInFire) {
            plant.markDead();
            // Drop nutrient ash into soil
            Cell cell = grid.getCellAtWorldCoords(plant.getPosition());
            if (cell != null) {
                cell.getSoilNutrients().replenish(25.0);
            }
        }
        EcosystemEventBus.getInstance().publish(EcosystemEvent.of(
                EcosystemEventType.ENVIRONMENTAL_DISASTER,
                "Wildfire burned " + caughtInFire.size() + " plants across sector",
                origin,
                totalTicks,
                null
        ));
    }

    public void triggerDrought(double severity) {
        for (int x = 0; x < grid.getCols(); x++) {
            for (int y = 0; y < grid.getRows(); y++) {
                Cell cell = grid.getCellAtGridCoords(x, y);
                if (cell != null) {
                    try {
                        cell.getSoilNutrients().consume(cell.getSoilNutrients().getCurrentAmount() * severity);
                    } catch (Exception ignored) {}
                }
            }
        }
        EcosystemEventBus.getInstance().publish(EcosystemEvent.of(
                EcosystemEventType.ENVIRONMENTAL_DISASTER,
                "Severe Drought depleted soil nutrients across all biomes",
                new Vector2D(config.worldWidth() / 2, config.worldHeight() / 2),
                totalTicks,
                null
        ));
    }

    public void triggerFertileBloom() {
        for (int x = 0; x < grid.getCols(); x++) {
            for (int y = 0; y < grid.getRows(); y++) {
                Cell cell = grid.getCellAtGridCoords(x, y);
                if (cell != null && cell.getBiome().isTraversableByLand()) {
                    cell.getSoilNutrients().replenish(40.0);
                }
            }
        }
        for (int i = 0; i < 25; i++) {
            Vector2D pos = findRandomValidPosition();
            if (pos != null) {
                try {
                    grid.addEntity(Plant.createDefault(pos));
                } catch (EntityOutOfBoundsException ignored) {}
            }
        }
        grid.synchronizeEntities();
        EcosystemEventBus.getInstance().publish(EcosystemEvent.of(
                EcosystemEventType.FERTILE_BLOOM,
                "Fertile Monsoon Bloom triggered rapid autotrophic proliferation",
                new Vector2D(config.worldWidth() / 2, config.worldHeight() / 2),
                totalTicks,
                null
        ));
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

    public void reset() {
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
        populateWorld();
        this.running = true;
    }
}
