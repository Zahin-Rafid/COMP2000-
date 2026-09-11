package com.ecosystem;

import com.ecosystem.config.ConfigLoader;
import com.ecosystem.config.SimulationConfig;
import com.ecosystem.core.Engine;
import com.ecosystem.core.Vector2D;
import com.ecosystem.exceptions.*;
import com.ecosystem.grid.BoundedResource;
import com.ecosystem.grid.SpatialGrid;
import com.ecosystem.model.Entity;
import com.ecosystem.model.entities.Herbivore;
import com.ecosystem.model.entities.Plant;
import com.ecosystem.model.genetics.Genome;

/**
 * Headless test harness verifying domain logic, exception propagation, and generics.
 */
public class EcosystemVerificationTest {
    public static void main(String[] args) {
        System.out.println("Starting Automated Ecosystem Logic Verification...");
        int passed = 0;
        int total = 5;

        // Test 1: BoundedResource Generics & Exception
        try {
            BoundedResource<Double> energy = new BoundedResource<>("Energy", 50.0, 10.0);
            double taken = energy.consume(5.0);
            assert Math.abs(taken - 5.0) < 0.001;
            energy.consume(10.0); // Will take remaining 5.0
            try {
                energy.consume(1.0); // Should throw ResourceDepletedException
                System.err.println("FAIL: Expected ResourceDepletedException");
            } catch (ResourceDepletedException e) {
                System.out.println("PASS: ResourceDepletedException thrown correctly: " + e.getMessage());
                passed++;
            }
        } catch (Exception e) {
            System.err.println("FAIL Test 1: " + e.getMessage());
        }

        // Test 2: SpatialGrid Generics & Out of Bounds Exception
        try {
            SpatialGrid<Entity> grid = new SpatialGrid<>(500.0, 500.0, 25.0);
            Plant p = Plant.createDefault(new Vector2D(100.0, 100.0));
            grid.addEntity(p);
            grid.synchronizeEntities();

            try {
                Plant outOfBounds = Plant.createDefault(new Vector2D(9999.0, 9999.0));
                grid.addEntity(outOfBounds);
                System.err.println("FAIL: Expected EntityOutOfBoundsException");
            } catch (EntityOutOfBoundsException e) {
                System.out.println("PASS: EntityOutOfBoundsException caught properly: " + e.getMessage());
                passed++;
            }
        } catch (Exception e) {
            System.err.println("FAIL Test 2: " + e.getMessage());
        }

        // Test 3: Genome validation & InvalidGenomeException
        try {
            try {
                new Genome(-50.0, 100.0, 5.0, 1.0, 0.1); // Negative speed
                System.err.println("FAIL: Expected InvalidGenomeException");
            } catch (InvalidGenomeException e) {
                System.out.println("PASS: InvalidGenomeException caught: " + e.getMessage());
                passed++;
            }
        } catch (Exception e) {
            System.err.println("FAIL Test 3: " + e.getMessage());
        }

        // Test 4: ConfigLoader malformed syntax check
        try {
            try {
                ConfigLoader.parseConfig("invalid_property_line_without_equals");
                System.err.println("FAIL: Expected InvalidConfigurationException");
            } catch (InvalidConfigurationException e) {
                System.out.println("PASS: InvalidConfigurationException caught: " + e.getMessage());
                passed++;
            }
        } catch (Exception e) {
            System.err.println("FAIL Test 4: " + e.getMessage());
        }

        // Test 5: Headless 300-tick simulation cycle
        try {
            SimulationConfig config = SimulationConfig.createDefault();
            Engine engine = new Engine(config);
            for (int i = 0; i < 300; i++) {
                engine.update(0.05);
            }
            int entityCount = engine.getGrid().getAllEntities().size();
            System.out.printf("PASS: Headless simulation completed 300 ticks smoothly. Active entities: %d%n", entityCount);
            passed++;
        } catch (Exception e) {
            System.err.println("FAIL Test 5: " + e.getMessage());
        }

        System.out.printf("%nResult: %d / %d Tests Passed Successfully.%n", passed, total);
        if (passed != total) {
            System.exit(1);
        }
    }
}
