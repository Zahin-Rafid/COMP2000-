package com.ecosystem.config;

import com.ecosystem.exceptions.InvalidConfigurationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Validates and loads simulation parameters from configuration streams.
 */
public class ConfigLoader {

    public static SimulationConfig parseConfig(String configData) throws InvalidConfigurationException {
        if (configData == null || configData.isBlank()) {
            throw new InvalidConfigurationException("Configuration data stream is empty or null.");
        }

        Map<String, String> properties = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new StringReader(configData))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue; // Skip comments and blank lines
                }
                int eqIdx = line.indexOf('=');
                if (eqIdx == -1) {
                    throw new InvalidConfigurationException(
                            String.format("Malformed configuration syntax at line %d: '%s'. Expected key=value.",
                                    lineNumber, line));
                }
                String key = line.substring(0, eqIdx).trim();
                String value = line.substring(eqIdx + 1).trim();
                properties.put(key, value);
            }
        } catch (IOException e) {
            throw new InvalidConfigurationException("Failed to read configuration stream", e);
        }

        try {
            double width = parseDouble(properties, "worldWidth", 1000.0, 300.0, 3000.0);
            double height = parseDouble(properties, "worldHeight", 700.0, 300.0, 2000.0);
            double cellSize = parseDouble(properties, "cellSize", 25.0, 10.0, 100.0);
            int plants = parseInt(properties, "initialPlants", 80, 0, 500);
            int herbivores = parseInt(properties, "initialHerbivores", 35, 0, 300);
            int carnivores = parseInt(properties, "initialCarnivores", 12, 0, 150);
            int apex = parseInt(properties, "initialApexPredators", 4, 0, 50);
            int decomposers = parseInt(properties, "initialDecomposers", 15, 0, 100);
            double regen = parseDouble(properties, "soilRegenRate", 0.8, 0.0, 10.0);

            return new SimulationConfig(width, height, cellSize, plants, herbivores, carnivores, apex, decomposers,
                    regen);
        } catch (NumberFormatException e) {
            throw new InvalidConfigurationException("Invalid numeric value in configuration parameter", e);
        }
    }

    private static double parseDouble(Map<String, String> map, String key, double defaultVal, double min, double max)
            throws InvalidConfigurationException {
        if (!map.containsKey(key))
            return defaultVal;
        double val = Double.parseDouble(map.get(key));
        if (val < min || val > max) {
            throw new InvalidConfigurationException(
                    String.format("Parameter '%s' value %.1f is out of bounds [%.1f, %.1f]", key, val, min, max));
        }
        return val;
    }

    private static int parseInt(Map<String, String> map, String key, int defaultVal, int min, int max)
            throws InvalidConfigurationException {
        if (!map.containsKey(key))
            return defaultVal;
        int val = Integer.parseInt(map.get(key));
        if (val < min || val > max) {
            throw new InvalidConfigurationException(
                    String.format("Parameter '%s' value %d is out of bounds [%d, %d]", key, val, min, max));
        }
        return val;
    }
}
