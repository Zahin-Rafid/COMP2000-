package com.ecosystem.model;

import com.ecosystem.core.Vector2D;
import com.ecosystem.exceptions.EntityOutOfBoundsException;
import com.ecosystem.grid.SpatialGrid;
import com.ecosystem.model.genetics.Genome;

import java.awt.Color;

/**
 * Abstract base class for living biological organisms requiring energy and metabolism.
 * Demonstrates inheritance hierarchy under Entity.
 */
public abstract class Organism extends Entity {
    private double energy;
    private final double maxEnergy;
    private double age;
    private final double maxAge;
    private final double reproductionThreshold;
    private final Genome genome;
    private final int generation;

    public Organism(Vector2D position, double radius, Color color, double initialEnergy,
                    double maxEnergy, double maxAge, double reproductionThreshold,
                    Genome genome, int generation) {
        super(position, radius, color);
        this.energy = initialEnergy;
        this.maxEnergy = maxEnergy;
        this.maxAge = maxAge;
        this.reproductionThreshold = reproductionThreshold;
        this.genome = genome;
        this.generation = generation;
        this.age = 0;
    }

    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double energy) {
        this.energy = Math.max(0, Math.min(maxEnergy, energy));
        if (this.energy <= 0) {
            markDead();
        }
    }

    public void addEnergy(double amount) {
        setEnergy(this.energy + amount);
    }

    public void consumeEnergy(double amount) {
        setEnergy(this.energy - amount);
    }

    public double getMaxEnergy() {
        return maxEnergy;
    }

    public double getAge() {
        return age;
    }

    public double getMaxAge() {
        return maxAge;
    }

    public double getReproductionThreshold() {
        return reproductionThreshold;
    }

    public Genome getGenome() {
        return genome;
    }

    public int getGeneration() {
        return generation;
    }

    public boolean canReproduce() {
        return energy >= reproductionThreshold && age >= (maxAge * 0.15) && age <= (maxAge * 0.85);
    }

    /**
     * Metabolic burn based on organism size, speed, and genome efficiency.
     */
    protected void burnBaseMetabolism(double deltaSeconds) {
        double cost = (genome.getSize() * 0.5 + 0.2) * genome.getMetabolismEfficiency() * deltaSeconds;
        consumeEnergy(cost);
        this.age += deltaSeconds;
        if (this.age >= maxAge) {
            markDead();
        }
    }

    /**
     * Polymorphic reproduction method creating an offspring organism.
     */
    public abstract Organism reproduce(SpatialGrid<Entity> grid) throws EntityOutOfBoundsException;
}
