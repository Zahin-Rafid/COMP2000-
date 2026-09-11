package com.ecosystem.model.genetics;

import java.util.Objects;
import java.util.Random;

/**
 * Generic container for a hereditary genetic trait.
 * Demonstrates generics with encapsulation of allele values and mutation variance.
 *
 * @param <T> The value type of the gene allele.
 */
public class Chromosome<T> {
    private final String traitName;
    private T allele;

    public Chromosome(String traitName, T initialValue) {
        this.traitName = Objects.requireNonNull(traitName, "Trait name cannot be null");
        this.allele = Objects.requireNonNull(initialValue, "Initial allele value cannot be null");
    }

    public String getTraitName() {
        return traitName;
    }

    public T getAllele() {
        return allele;
    }

    public void setAllele(T allele) {
        this.allele = Objects.requireNonNull(allele, "Allele cannot be set to null");
    }

    /**
     * Mutates a double-valued chromosome by adding Gaussian variance.
     */
    public void mutateDouble(Random random, double mutationRate, double stdDev, double minVal, double maxVal) {
        if (allele instanceof Double current) {
            if (random.nextDouble() < mutationRate) {
                double delta = random.nextGaussian() * stdDev;
                double mutated = Math.max(minVal, Math.min(maxVal, current + delta));
                @SuppressWarnings("unchecked")
                T updated = (T) Double.valueOf(mutated);
                this.allele = updated;
            }
        }
    }

    @Override
    public String toString() {
        return String.format("%s: %s", traitName, allele);
    }
}
