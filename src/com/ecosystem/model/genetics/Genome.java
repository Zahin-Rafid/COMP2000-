package com.ecosystem.model.genetics;

import com.ecosystem.exceptions.InvalidGenomeException;

import java.util.Random;

/**
 * Encapsulates the complete set of hereditary traits for an organism.
 * Handles chromosome mutation, crossover, and phenotypic validation.
 */
public class Genome {
    private static final Random RNG = new Random();

    private final Chromosome<Double> speedGene;
    private final Chromosome<Double> visionRadiusGene;
    private final Chromosome<Double> sizeGene;
    private final Chromosome<Double> metabolismEfficiencyGene;
    private final Chromosome<Double> mutationRateGene;

    public Genome(double speed, double visionRadius, double size, double metabolismEfficiency, double mutationRate)
            throws InvalidGenomeException {
        this.speedGene = new Chromosome<>("Speed", speed);
        this.visionRadiusGene = new Chromosome<>("VisionRadius", visionRadius);
        this.sizeGene = new Chromosome<>("Size", size);
        this.metabolismEfficiencyGene = new Chromosome<>("MetabolismEfficiency", metabolismEfficiency);
        this.mutationRateGene = new Chromosome<>("MutationRate", mutationRate);

        validate();
    }

    public double getSpeed() {
        return speedGene.getAllele();
    }

    public double getVisionRadius() {
        return visionRadiusGene.getAllele();
    }

    public double getSize() {
        return sizeGene.getAllele();
    }

    public double getMetabolismEfficiency() {
        return metabolismEfficiencyGene.getAllele();
    }

    public double getMutationRate() {
        return mutationRateGene.getAllele();
    }

    public void validate() throws InvalidGenomeException {
        if (Double.isNaN(getSpeed()) || getSpeed() < 0.0 || getSpeed() > 300.0) {
            throw new InvalidGenomeException("Speed", getSpeed(), "Speed must be between 0.0 and 300.0");
        }
        if (Double.isNaN(getVisionRadius()) || getVisionRadius() < 5.0 || getVisionRadius() > 500.0) {
            throw new InvalidGenomeException("VisionRadius", getVisionRadius(), "Vision radius must be between 5.0 and 500.0");
        }
        if (Double.isNaN(getSize()) || getSize() < 1.0 || getSize() > 50.0) {
            throw new InvalidGenomeException("Size", getSize(), "Size must be between 1.0 and 50.0");
        }
        if (Double.isNaN(getMetabolismEfficiency()) || getMetabolismEfficiency() < 0.1 || getMetabolismEfficiency() > 5.0) {
            throw new InvalidGenomeException("MetabolismEfficiency", getMetabolismEfficiency(), "Metabolism efficiency must be between 0.1 and 5.0");
        }
        if (Double.isNaN(getMutationRate()) || getMutationRate() < 0.0 || getMutationRate() > 1.0) {
            throw new InvalidGenomeException("MutationRate", getMutationRate(), "Mutation rate must be between 0.0 and 1.0");
        }
    }

    /**
     * Creates an offspring genome with mutations applied according to the parent mutation rate.
     */
    public Genome mutate() throws InvalidGenomeException {
        double rate = getMutationRate();
        Genome child = new Genome(
                getSpeed(),
                getVisionRadius(),
                getSize(),
                getMetabolismEfficiency(),
                getMutationRate()
        );

        child.speedGene.mutateDouble(RNG, rate, 5.0, 10.0, 180.0);
        child.visionRadiusGene.mutateDouble(RNG, rate, 8.0, 15.0, 300.0);
        child.sizeGene.mutateDouble(RNG, rate, 0.4, 2.0, 18.0);
        child.metabolismEfficiencyGene.mutateDouble(RNG, rate, 0.05, 0.3, 2.0);
        child.mutationRateGene.mutateDouble(RNG, rate * 0.5, 0.02, 0.01, 0.5);

        child.validate();
        return child;
    }
}
