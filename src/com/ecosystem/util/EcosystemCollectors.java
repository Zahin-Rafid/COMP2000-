package com.ecosystem.util;

import com.ecosystem.model.Entity;
import com.ecosystem.model.Organism;
import com.ecosystem.model.decorator.OrganismDecorator;
import com.ecosystem.model.entities.*;

import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collector;

/**
 * Utility class providing simulation-specific custom collectors built with
 * {@code Collector.of()}.
 * Demonstrates advanced functional aggregation and thread-safe combiners for
 * parallel streams.
 */
public final class EcosystemCollectors {

    private EcosystemCollectors() {
    }

    /**
     * Custom collector accumulating entities into a holistic
     * {@link EcosystemSummary} in a single pass.
     * Fully associative combiner guarantees thread-safety during parallel stream
     * reduction.
     */
    public static Collector<Entity, ?, EcosystemSummary> toEcosystemSummary() {
        return Collector.of(
                Accumulator::new,
                Accumulator::accumulate,
                Accumulator::merge,
                Accumulator::finish);
    }

    private static class Accumulator {
        private final Map<String, Long> speciesCounts = new HashMap<>();
        private final DoubleSummaryStatistics energyStats = new DoubleSummaryStatistics();
        private final DoubleSummaryStatistics speedStats = new DoubleSummaryStatistics();
        private final DoubleSummaryStatistics visionStats = new DoubleSummaryStatistics();

        private double totalBiomass = 0.0;
        private double primaryProducersBiomass = 0.0;
        private double consumersBiomass = 0.0;
        private double predatorsBiomass = 0.0;
        private long totalOrganisms = 0;

        void accumulate(Entity entity) {
            if (!entity.isAlive())
                return;

            Entity resolved = (entity instanceof OrganismDecorator dec) ? dec.unwrap() : entity;
            String species = resolved.getClass().getSimpleName();
            speciesCounts.put(species, speciesCounts.getOrDefault(species, 0L) + 1L);

            if (resolved instanceof Organism org) {
                totalOrganisms++;
                double energy = org.getEnergy();
                energyStats.accept(energy);
                totalBiomass += energy;

                speedStats.accept(org.getGenome().getSpeed());
                visionStats.accept(org.getGenome().getVisionRadius());

                if (org instanceof Plant) {
                    primaryProducersBiomass += energy;
                } else if (org instanceof Herbivore) {
                    consumersBiomass += energy;
                } else if (org instanceof Carnivore || org instanceof ApexPredator) {
                    predatorsBiomass += energy;
                }
            } else if (resolved instanceof Carcass carcass) {
                totalBiomass += carcass.getBiomass();
            }
        }

        Accumulator merge(Accumulator other) {
            other.speciesCounts.forEach((species, count) -> this.speciesCounts.put(species,
                    this.speciesCounts.getOrDefault(species, 0L) + count));

            this.energyStats.combine(other.energyStats);
            this.speedStats.combine(other.speedStats);
            this.visionStats.combine(other.visionStats);

            this.totalBiomass += other.totalBiomass;
            this.primaryProducersBiomass += other.primaryProducersBiomass;
            this.consumersBiomass += other.consumersBiomass;
            this.predatorsBiomass += other.predatorsBiomass;
            this.totalOrganisms += other.totalOrganisms;

            return this;
        }

        EcosystemSummary finish() {
            // Compute Shannon Diversity Index over living organism species: H' = -sum(p_i * ln(p_i))
            double shannonIndex = 0.0;
            if (totalOrganisms > 0) {
                for (Map.Entry<String, Long> entry : speciesCounts.entrySet()) {
                    if ("Carcass".equals(entry.getKey())) continue;
                    long count = entry.getValue();
                    if (count > 0) {
                        double p = (double) count / totalOrganisms;
                        shannonIndex -= p * Math.log(p);
                    }
                }
            }

            return new EcosystemSummary(
                    new HashMap<>(speciesCounts),
                    totalOrganisms,
                    totalBiomass,
                    energyStats.getCount() > 0 ? energyStats.getAverage() : 0.0,
                    shannonIndex,
                    speedStats.getCount() > 0 ? speedStats.getAverage() : 0.0,
                    visionStats.getCount() > 0 ? visionStats.getAverage() : 0.0,
                    primaryProducersBiomass,
                    consumersBiomass,
                    predatorsBiomass);
        }
    }
}
