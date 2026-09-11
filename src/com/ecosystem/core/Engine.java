package com.ecosystem.core;

import com.ecosystem.config.SimulationConfig;
import com.ecosystem.exceptions.EntityOutOfBoundsException;
import com.ecosystem.grid.Cell;
import com.ecosystem.grid.SpatialGrid;
import com.ecosystem.model.Entity;
import com.ecosystem.model.Organism;
import com.ecosystem.model.entities.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Main simulation coordinator driving time steps, entity updates, and ecosystem statistics.
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
    private int highestGeneration;

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
        this.highestGeneration = 1;

        populateWorld();
    }

    public void populateWorld() {
        // Spawn plants
        for (int i = 0; i < config.initialPlants(); i++) {
            Vector2D pos = findRandomValidPosition();
            if (pos != null) {
                try {
                    grid.addEntity(Plant.createDefault(pos));
                } catch (EntityOutOfBoundsException ignored) {}
            }
        }

        // Spawn herbivores
        for (int i = 0; i < config.initialHerbivores(); i++) {
            Vector2D pos = findRandomValidPosition();
            if (pos != null) {
                try {
                    grid.addEntity(Herbivore.createDefault(pos));
                } catch (EntityOutOfBoundsException ignored) {}
            }
        }

        // Spawn carnivores
        for (int i = 0; i < config.initialCarnivores(); i++) {
            Vector2D pos = findRandomValidPosition();
            if (pos != null) {
                try {
                    grid.addEntity(Carnivore.createDefault(pos));
                } catch (EntityOutOfBoundsException ignored) {}
            }
        }

        // Spawn apex predators
        for (int i = 0; i < config.initialApexPredators(); i++) {
            Vector2D pos = findRandomValidPosition();
            if (pos != null) {
                try {
                    grid.addEntity(ApexPredator.createDefault(pos));
                } catch (EntityOutOfBoundsException ignored) {}
            }
        }

        // Spawn decomposers
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

    public synchronized void update(double deltaSeconds) {
        if (!running) return;

        double effectiveDelta = deltaSeconds * speedMultiplier;
        totalSimulatedTime += effectiveDelta;
        totalTicks++;

        // 1. Natural environmental regeneration
        grid.updateEnvironment(config.soilRegenRate() * effectiveDelta);

        // 2. Entity life cycles & actions
        List<Entity> currentEntities = new ArrayList<>(grid.getAllEntities());
        for (Entity entity : currentEntities) {
            if (entity.isAlive()) {
                try {
                    entity.update(grid, effectiveDelta);
                } catch (EntityOutOfBoundsException e) {
                    System.err.println("Handling entity bounds error: " + e.getMessage());
                    // Clamp position safely back into bounds
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

        // 3. Synchronize births and deaths
        grid.synchronizeEntities();

        // 4. Record statistics every 30 ticks
        if (totalTicks % 30 == 0) {
            recordStatistics();
        }
    }

    private void recordStatistics() {
        int plants = grid.queryEntities(Plant.class).size();
        int herbivores = grid.queryEntities(Herbivore.class).size();
        int carnivores = grid.queryEntities(Carnivore.class).size();
        int apex = grid.queryEntities(ApexPredator.class).size();

        plantHistory.add(plants);
        herbivoreHistory.add(herbivores);
        carnivoreHistory.add(carnivores);
        apexHistory.add(apex);

        // Limit graph history size to last 200 points
        if (plantHistory.size() > 200) {
            plantHistory.remove(0);
            herbivoreHistory.remove(0);
            carnivoreHistory.remove(0);
            apexHistory.remove(0);
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

    public void reset() {
        this.running = false;
        List<Entity> all = new ArrayList<>(grid.getAllEntities());
        for (Entity e : all) {
            e.markDead();
        }
        grid.synchronizeEntities();
        plantHistory.clear();
        herbivoreHistory.clear();
        carnivoreHistory.clear();
        apexHistory.clear();
        highestGeneration = 1;
        totalTicks = 0;
        totalSimulatedTime = 0.0;
        populateWorld();
        this.running = true;
    }
}
