package com.ecosystem.grid;

import java.awt.Color;

/**
 * Enumeration of biome environmental types with display colors and fertility multipliers.
 */
public enum BiomeType {
    WATER(new Color(45, 115, 185), 0.0, false),
    FERTILE_SOIL(new Color(60, 140, 60), 1.2, true),
    FOREST(new Color(35, 100, 45), 1.5, true),
    DESERT(new Color(210, 180, 110), 0.3, true);

    private final Color color;
    private final double fertilityFactor;
    private final boolean traversableByLand;

    BiomeType(Color color, double fertilityFactor, boolean traversableByLand) {
        this.color = color;
        this.fertilityFactor = fertilityFactor;
        this.traversableByLand = traversableByLand;
    }

    public Color getColor() {
        return color;
    }

    public double getFertilityFactor() {
        return fertilityFactor;
    }

    public boolean isTraversableByLand() {
        return traversableByLand;
    }
}
