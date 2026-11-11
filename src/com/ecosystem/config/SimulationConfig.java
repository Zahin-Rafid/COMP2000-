package com.ecosystem.config;

/**
 * Fixed configuration parameters for the ecosystem simulation.
 */
public record SimulationConfig(
        double worldWidth,
        double worldHeight,
        double cellSize,
        int initialPlants,
        int initialHerbivores,
        int initialCarnivores,
        int initialApexPredators,
        int initialDecomposers,
        double soilRegenRate) {
    public static SimulationConfig createDefault() {
        return new SimulationConfig(
                1000.0,
                700.0,
                25.0,
                110,
                32,
                8,
                3,
                12,
                1.6);
    }
}
