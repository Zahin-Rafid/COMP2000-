package com.ecosystem.grid;

import com.ecosystem.core.Vector2D;
import com.ecosystem.exceptions.ResourceDepletedException;

/**
 * Represents an individual spatial tile in the simulation grid containing soil fertility,
 * moisture, and biome classification.
 */
public class Cell {
    private final int gridX;
    private final int gridY;
    private final Vector2D worldCenter;
    private BiomeType biome;
    private final BoundedResource<Double> soilNutrients;
    private final BoundedResource<Double> moisture;

    public Cell(int gridX, int gridY, double cellSize, BiomeType biome) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.worldCenter = new Vector2D((gridX + 0.5) * cellSize, (gridY + 0.5) * cellSize);
        this.biome = biome;

        double maxNutrients = 100.0 * biome.getFertilityFactor();
        this.soilNutrients = new BoundedResource<>("Nutrients", Math.max(10.0, maxNutrients), maxNutrients * 0.8);
        this.moisture = new BoundedResource<>("Moisture", 100.0, biome == BiomeType.DESERT ? 20.0 : 75.0);
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public Vector2D getWorldCenter() {
        return worldCenter;
    }

    public BiomeType getBiome() {
        return biome;
    }

    public void setBiome(BiomeType biome) {
        this.biome = biome;
    }

    public BoundedResource<Double> getSoilNutrients() {
        return soilNutrients;
    }

    public BoundedResource<Double> getMoisture() {
        return moisture;
    }

    public void update(double naturalRegenRate) {
        if (biome != BiomeType.WATER) {
            soilNutrients.replenish(naturalRegenRate * biome.getFertilityFactor());
        }
    }

    public double extractNutrients(double requestedAmount) throws ResourceDepletedException {
        return soilNutrients.consume(requestedAmount);
    }
}
