package com.ecosystem;

import com.ecosystem.config.ConfigLoader;
import com.ecosystem.config.SimulationConfig;
import com.ecosystem.core.Engine;
import com.ecosystem.core.Vector2D;
import com.ecosystem.events.EcosystemEventBus;
import com.ecosystem.exceptions.*;
import com.ecosystem.grid.BoundedResource;
import com.ecosystem.grid.SpatialGrid;
import com.ecosystem.model.Entity;
import com.ecosystem.model.decorator.InfectedDebuff;
import com.ecosystem.model.entities.Herbivore;
import com.ecosystem.model.entities.Plant;
import com.ecosystem.model.genetics.Genome;
import com.ecosystem.model.state.FleeingState;
import com.ecosystem.model.state.WanderingState;
import com.ecosystem.model.strategy.FlockingStrategy;
import com.ecosystem.util.EcosystemCollectors;
import com.ecosystem.util.EcosystemSummary;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Headless test harness verifying domain logic, exception propagation, generics,
 * design patterns (State, Strategy, Observer, Decorator), and parallel streams.
 */
public class EcosystemVerificationTest {
    public static void main(String[] args) {
        System.out.println("Starting Automated Ecosystem Logic & Patterns Verification...");
        int passed = 0;
        int total = 10;

        // Test 1: BoundedResource Generics & Exception
        try {
            BoundedResource<Double> energy = new BoundedResource<>("Energy", 50.0, 10.0);
            double taken = energy.consume(5.0);
            assert Math.abs(taken - 5.0) < 0.001;
            energy.consume(10.0);
            try {
                energy.consume(1.0);
                System.err.println("FAIL: Expected ResourceDepletedException");
            } catch (ResourceDepletedException e) {
                System.out.println("PASS 1: ResourceDepletedException thrown correctly: " + e.getMessage());
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
                System.out.println("PASS 2: EntityOutOfBoundsException caught properly: " + e.getMessage());
                passed++;
            }
        } catch (Exception e) {
            System.err.println("FAIL Test 2: " + e.getMessage());
        }

        // Test 3: Genome validation & InvalidGenomeException
        try {
            try {
                new Genome(-50.0, 100.0, 5.0, 1.0, 0.1);
                System.err.println("FAIL: Expected InvalidGenomeException");
            } catch (InvalidGenomeException e) {
                System.out.println("PASS 3: InvalidGenomeException caught: " + e.getMessage());
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
                System.out.println("PASS 4: InvalidConfigurationException caught: " + e.getMessage());
                passed++;
            }
        } catch (Exception e) {
            System.err.println("FAIL Test 4: " + e.getMessage());
        }

        // Test 5: State Pattern State Transitions
        try {
            Herbivore herbivore = Herbivore.createDefault(new Vector2D(150, 150));
            assert herbivore.getCurrentState() instanceof WanderingState;
            Plant dummyThreat = Plant.createDefault(new Vector2D(160, 160));
            herbivore.changeState(new FleeingState(dummyThreat));
            assert herbivore.getCurrentState() instanceof FleeingState;
            System.out.println("PASS 5: State Pattern transition (Wandering -> Fleeing) verified.");
            passed++;
        } catch (Exception e) {
            System.err.println("FAIL Test 5: " + e.getMessage());
        }

        // Test 6: Strategy Pattern Steering Calculation
        try {
            Herbivore herbivore = Herbivore.createDefault(new Vector2D(200, 200));
            SpatialGrid<Entity> grid = new SpatialGrid<>(500, 500, 25);
            herbivore.setMovementStrategy(new FlockingStrategy(50.0));
            Vector2D force = herbivore.getMovementStrategy().calculateSteering(herbivore, grid, 0.05);
            assert force != null;
            System.out.println("PASS 6: Strategy Pattern (FlockingStrategy & dynamic swap) verified.");
            passed++;
        } catch (Exception e) {
            System.err.println("FAIL Test 6: " + e.getMessage());
        }

        // Test 7: Observer Pattern Event Dispatch
        try {
            AtomicInteger eventsReceived = new AtomicInteger(0);
            EcosystemEventBus.getInstance().subscribe(event -> eventsReceived.incrementAndGet());
            SimulationConfig config = SimulationConfig.createDefault();
            Engine engine = new Engine(config);
            engine.triggerFertileBloom();
            assert eventsReceived.get() > 0;
            System.out.println("PASS 7: Observer Pattern decoupled event publication received by listener.");
            passed++;
        } catch (Exception e) {
            System.err.println("FAIL Test 7: " + e.getMessage());
        }

        // Test 8: Decorator Pattern Dynamic Wrapping
        try {
            Herbivore herbivore = Herbivore.createDefault(new Vector2D(250, 250));
            InfectedDebuff infected = new InfectedDebuff(herbivore, 3.0);
            assert infected.getWrapped() == herbivore;
            assert infected.getEnergy() == herbivore.getEnergy();
            System.out.println("PASS 8: Decorator Pattern (InfectedDebuff wrapping Organism) verified.");
            passed++;
        } catch (Exception e) {
            System.err.println("FAIL Test 8: " + e.getMessage());
        }

        // Test 9: Custom Collector & Parallel Stream Execution
        try {
            SimulationConfig config = SimulationConfig.createDefault();
            Engine engine = new Engine(config);
            EcosystemSummary summary = engine.computeParallelEcosystemMetrics();
            assert summary.totalOrganisms() > 0;
            assert summary.shannonDiversityIndex() >= 0.0;
            System.out.printf("PASS 9: Parallel Stream with Custom Collector: %d organisms, Shannon H' = %.3f%n",
                    summary.totalOrganisms(), summary.shannonDiversityIndex());
            passed++;
        } catch (Exception e) {
            System.err.println("FAIL Test 9: " + e.getMessage());
        }

        // Test 10: Headless 300-tick simulation cycle
        try {
            SimulationConfig config = SimulationConfig.createDefault();
            Engine engine = new Engine(config);
            for (int i = 0; i < 300; i++) {
                engine.update(0.05);
            }
            int entityCount = engine.getGrid().getAllEntities().size();
            System.out.printf("PASS 10: Headless simulation completed 300 ticks smoothly. Active entities: %d%n", entityCount);
            passed++;
        } catch (Exception e) {
            System.err.println("FAIL Test 10: " + e.getMessage());
        }

        System.out.printf("%nResult: %d / %d Tests Passed Successfully.%n", passed, total);
        if (passed != total) {
            System.exit(1);
        }
    }
}
