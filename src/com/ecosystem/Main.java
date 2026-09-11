package com.ecosystem;

import com.ecosystem.config.ConfigLoader;
import com.ecosystem.config.SimulationConfig;
import com.ecosystem.core.Engine;
import com.ecosystem.exceptions.InvalidConfigurationException;
import com.ecosystem.ui.SimulationFrame;

import javax.swing.SwingUtilities;

/**
 * Application entry point for the COMP2000 Multi-Tier Ecosystem Simulation.
 */
public class Main {
    private static final String DEFAULT_CONFIG_DATA = """
            # World Geometry
            worldWidth=1000.0
            worldHeight=700.0
            cellSize=25.0
            
            # Initial Population Distribution
            initialPlants=85
            initialHerbivores=35
            initialCarnivores=12
            initialApexPredators=4
            initialDecomposers=15
            
            # Environment Parameters
            soilRegenRate=0.8
            """;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("COMP2000: Multi-Tier Ecosystem & Evolution System");
        System.out.println("==================================================");

        SimulationConfig config;
        try {
            System.out.println("Loading and validating simulation configuration...");
            config = ConfigLoader.parseConfig(DEFAULT_CONFIG_DATA);
            System.out.println("Configuration successfully verified: " + config);
        } catch (InvalidConfigurationException e) {
            System.err.println("Fatal Error during configuration bootstrap: " + e.getMessage());
            System.err.println("Falling back to safe internal defaults.");
            config = SimulationConfig.createDefault();
        }

        SimulationConfig finalConfig = config;
        SwingUtilities.invokeLater(() -> {
            Engine engine = new Engine(finalConfig);
            SimulationFrame frame = new SimulationFrame(engine);
            frame.startSimulation();
            System.out.println("Simulation graphical window active and running.");
        });
    }
}
