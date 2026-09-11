package com.ecosystem.model.entities;

import com.ecosystem.core.Vector2D;
import com.ecosystem.exceptions.EntityOutOfBoundsException;
import com.ecosystem.exceptions.InvalidGenomeException;
import com.ecosystem.exceptions.ResourceDepletedException;
import com.ecosystem.grid.Cell;
import com.ecosystem.grid.SpatialGrid;
import com.ecosystem.model.Entity;
import com.ecosystem.model.Organism;
import com.ecosystem.model.genetics.Genome;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

/**
 * Autotrophic organism that converts soil fertility and sunlight into energy.
 * Seeds offspring into neighboring tiles upon reaching energy thresholds.
 */
public class Plant extends Organism {
    private static final Random RNG = new Random();

    public Plant(Vector2D position, double initialEnergy, Genome genome, int generation) {
        super(
                position,
                Math.max(3.0, genome.getSize()),
                new Color(34, 177, 76),
                initialEnergy,
                100.0,
                80.0,  // Max age in seconds
                60.0,  // Reproduction threshold
                genome,
                generation
        );
    }

    public static Plant createDefault(Vector2D position) {
        try {
            Genome defaultGenome = new Genome(0.0, 10.0, 4.0, 0.8, 0.05);
            return new Plant(position, 25.0, defaultGenome, 1);
        } catch (InvalidGenomeException e) {
            throw new RuntimeException("Default plant genome initialization failed", e);
        }
    }

    @Override
    public void update(SpatialGrid<Entity> grid, double deltaSeconds) throws EntityOutOfBoundsException {
        if (!isAlive()) return;

        burnBaseMetabolism(deltaSeconds * 0.2);

        Cell cell = grid.getCellAtWorldCoords(getPosition());
        if (cell != null && cell.getBiome().isTraversableByLand()) {
            double rate = 4.0 * cell.getBiome().getFertilityFactor() * deltaSeconds;
            try {
                double extracted = cell.extractNutrients(rate);
                addEnergy(extracted);
            } catch (ResourceDepletedException e) {
                // Soil is temporarily depleted; plant rests without dying immediately
            }
        } else if (cell != null && !cell.getBiome().isTraversableByLand()) {
            // Drowning in water
            consumeEnergy(deltaSeconds * 10.0);
        }

        // Adjust radius visually with energy
        setRadius(Math.max(2.5, Math.min(8.0, 2.0 + (getEnergy() / getMaxEnergy()) * 5.0)));

        if (canReproduce()) {
            Plant child = reproduce(grid);
            if (child != null) {
                grid.addEntity(child);
                consumeEnergy(getReproductionThreshold() * 0.5);
            }
        }
    }

    @Override
    public Plant reproduce(SpatialGrid<Entity> grid) {
        try {
            double spreadRadius = 25.0 + RNG.nextDouble() * 30.0;
            double angle = RNG.nextDouble() * 2 * Math.PI;
            double newX = getPosition().getX() + Math.cos(angle) * spreadRadius;
            double newY = getPosition().getY() + Math.sin(angle) * spreadRadius;
            Vector2D childPos = new Vector2D(newX, newY);

            if (!grid.isWithinBounds(childPos)) {
                return null;
            }

            Cell cell = grid.getCellAtWorldCoords(childPos);
            if (cell == null || !cell.getBiome().isTraversableByLand()) {
                return null;
            }

            // Ensure not too overcrowded
            long nearbyPlants = grid.queryEntitiesNear(childPos, 15.0, Plant.class).size();
            if (nearbyPlants > 3) {
                return null;
            }

            Genome childGenome = getGenome().mutate();
            return new Plant(childPos, 15.0, childGenome, getGeneration() + 1);
        } catch (InvalidGenomeException e) {
            return null; // Bad mutation filtered out
        }
    }

    @Override
    public void render(Graphics2D g) {
        if (!isAlive()) return;
        int r = (int) getRadius();
        int x = (int) (getPosition().getX() - r);
        int y = (int) (getPosition().getY() - r);

        // Gradient green based on energy
        float energyRatio = (float) (getEnergy() / getMaxEnergy());
        int greenVal = (int) (120 + energyRatio * 135);
        greenVal = Math.min(255, Math.max(0, greenVal));
        g.setColor(new Color(30, greenVal, 40));
        g.fillOval(x, y, r * 2, r * 2);

        g.setColor(new Color(20, 80, 20));
        g.drawOval(x, y, r * 2, r * 2);
    }
}
