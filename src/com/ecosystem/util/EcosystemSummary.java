package com.ecosystem.util;

import java.util.Collections;
import java.util.Map;

/**
 * Immutable summary record containing aggregated ecological, thermodynamic,
 * and biodiversity metrics computed across the simulation world.
 */
public record EcosystemSummary(
        Map<String, Long> speciesCounts,
        long totalOrganisms,
        double totalBiomass,
        double averageEnergy,
        double shannonDiversityIndex,
        double averageSpeed,
        double averageVisionRadius,
        double primaryProducersBiomass,
        double consumersBiomass,
        double predatorsBiomass) {
    public EcosystemSummary {
        speciesCounts = Collections.unmodifiableMap(speciesCounts);
    }
}
