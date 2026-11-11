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
import com.ecosystem.model.decorator.AdrenalineBuff;
import com.ecosystem.model.decorator.CamouflageBuff;
import com.ecosystem.model.decorator.InfectedDebuff;
import com.ecosystem.model.entities.Herbivore;
import com.ecosystem.model.entities.Plant;
import com.ecosystem.model.genetics.Genome;
import com.ecosystem.model.state.FleeingState;
import com.ecosystem.model.state.RestingState;
import com.ecosystem.model.state.WanderingState;
import com.ecosystem.model.strategy.FlockingStrategy;
import com.ecosystem.util.EcosystemSummary;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Comprehensive headless verification suite covering domain exceptions, generics,
 * design patterns (State, Strategy, Observer, Decorator), parallel streams,
 * interactive controls, and multi-tick ecosystem stability.
 */
public class EcosystemVerificationTest {
    public static void main(String[] args) {
        System.out.println("Starting Comprehensive Ecosystem Logic & Controls Verification...");
        int passed = 0;
        int total = 12;

        // Test 1: BoundedResource Generics & Exception
        try {
            BoundedResource<Double> energy = new BoundedResource<>("Energy", 50.0, 10.0);
            double taken = energy.consume(5.0);
            if (Math.abs(taken - 5.0) >= 0.001) throw new AssertionError("Consume mismatch");
            energy.consume(10.0);
            try {
                energy.consume(1.0);
                System.err.println("FAIL 1: Expected ResourceDepletedException");
            } catch (ResourceDepletedException e) {
                System.out.println("PASS 1: ResourceDepletedException thrown correctly.");
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
                System.err.println("FAIL 2: Expected EntityOutOfBoundsException");
            } catch (EntityOutOfBoundsException e) {
                System.out.println("PASS 2: EntityOutOfBoundsException caught properly.");
                passed++;
            }
        } catch (Exception e) {
            System.err.println("FAIL Test 2: " + e.getMessage());
        }

        // Test 3: Genome validation & InvalidGenomeException
        try {
            try {
                new Genome(-50.0, 100.0, 5.0, 1.0, 0.1);
                System.err.println("FAIL 3: Expected InvalidGenomeException");
            } catch (InvalidGenomeException e) {
                System.out.println("PASS 3: InvalidGenomeException caught.");
                passed++;
            }
        } catch (Exception e) {
            System.err.println("FAIL Test 3: " + e.getMessage());
        }

        // Test 4: ConfigLoader malformed syntax check
        try {
            try {
                ConfigLoader.parseConfig("invalid_property_line_without_equals");
                System.err.println("FAIL 4: Expected InvalidConfigurationException");
            } catch (InvalidConfigurationException e) {
                System.out.println("PASS 4: InvalidConfigurationException caught.");
                passed++;
            }
        } catch (Exception e) {
            System.err.println("FAIL Test 4: " + e.getMessage());
        }

        // Test 5: State Pattern Transitions (Wandering -> Fleeing -> Resting -> Wandering)
        try {
            SpatialGrid<Entity> grid = new SpatialGrid<>(500, 500, 25);
            Herbivore herbivore = Herbivore.createDefault(new Vector2D(150, 150));
            if (!(herbivore.getCurrentState() instanceof WanderingState)) throw new AssertionError("Initial state");
            Plant dummyThreat = Plant.createDefault(new Vector2D(160, 160));
            herbivore.changeState(new FleeingState(dummyThreat));
            if (!(herbivore.getCurrentState() instanceof FleeingState)) throw new AssertionError("Fleeing state");
            herbivore.changeState(new RestingState());
            for (int i = 0; i < 45; i++) {
                herbivore.update(grid, 0.05);
            }
            if (!(herbivore.getCurrentState() instanceof WanderingState)) throw new AssertionError("Resting recovery");
            System.out.println("PASS 5: State Pattern lifecycle transitions verified.");
            passed++;
        } catch (Exception e) {
            System.err.println("FAIL Test 5: " + e.getMessage());
        }

        // Test 6: Strategy Pattern Steering & Lambda Substitution
        try {
            Herbivore herbivore = Herbivore.createDefault(new Vector2D(200, 200));
            SpatialGrid<Entity> grid = new SpatialGrid<>(500, 500, 25);
            herbivore.setMovementStrategy(new FlockingStrategy(50.0));
            Vector2D f1 = herbivore.getMovementStrategy().calculateSteering(herbivore, grid, 0.05);
            herbivore.setMovementStrategy((a, g, dt) -> a.calculateSteeringForce(new Vector2D(300, 300), 1.0));
            Vector2D f2 = herbivore.getMovementStrategy().calculateSteering(herbivore, grid, 0.05);
            if (f1 == null || f2 == null) throw new AssertionError("Null force");
            System.out.println("PASS 6: Strategy Pattern (FlockingStrategy & lambda substitution) verified.");
            passed++;
        } catch (Exception e) {
            System.err.println("FAIL Test 6: " + e.getMessage());
        }

        // Test 7: Observer Pattern Event Dispatch & Stamping
        try {
            AtomicInteger eventsReceived = new AtomicInteger(0);
            EcosystemEventBus.getInstance().subscribe(event -> eventsReceived.incrementAndGet());
            Engine engine = new Engine(SimulationConfig.createDefault());
            engine.update(0.05);
            engine.triggerFertileBloom();
            if (eventsReceived.get() <= 0) throw new AssertionError("No events received");
            System.out.println("PASS 7: Observer Pattern decoupled event publication verified.");
            passed++;
        } catch (Exception e) {
            System.err.println("FAIL Test 7: " + e.getMessage());
        }

        // Test 8: Decorator Pattern Wrapping & SpatialGrid Unwrapping
        try {
            SpatialGrid<Entity> grid = new SpatialGrid<>(500, 500, 25);
            Herbivore herbivore = Herbivore.createDefault(new Vector2D(250, 250));
            InfectedDebuff infected = new InfectedDebuff(new CamouflageBuff(herbivore, 0.5), 3.0);
            grid.addEntity(infected);
            grid.synchronizeEntities();

            List<Herbivore> queriedHerbs = grid.queryEntities(Herbivore.class);
            if (queriedHerbs.size() != 1 || queriedHerbs.get(0) != herbivore) {
                throw new AssertionError("Decorator unwrap in SpatialGrid failed");
            }
            if (infected.unwrap() != herbivore) throw new AssertionError("unwrap() mismatch");
            System.out.println("PASS 8: Decorator Pattern chaining & SpatialGrid unwrapping verified.");
            passed++;
        } catch (Exception e) {
            System.err.println("FAIL Test 8: " + e.getMessage());
        }

        // Test 9: Parallel Streams + Custom Collector with Decorated Entities
        try {
            Engine engine = new Engine(SimulationConfig.createDefault());
            engine.infectRandomOrganism();
            engine.buffRandomOrganism();
            EcosystemSummary summary = engine.computeParallelEcosystemMetrics();
            if (summary.totalOrganisms() <= 0 || summary.shannonDiversityIndex() <= 0.0) {
                throw new AssertionError("Summary calculation invalid");
            }
            System.out.printf("PASS 9: Parallel Stream Custom Collector: %d organisms, Shannon H' = %.3f%n",
                    summary.totalOrganisms(), summary.shannonDiversityIndex());
            passed++;
        } catch (Exception e) {
            System.err.println("FAIL Test 9: " + e.getMessage());
        }

        // Test 10: Interactive Pause, Step, Spawn, Disasters & Reset
        try {
            Engine engine = new Engine(SimulationConfig.createDefault());
            engine.setRunning(false);
            long tickBefore = engine.getTotalTicks();
            engine.update(0.05); // Should not advance while paused
            if (engine.getTotalTicks() != tickBefore) throw new AssertionError("Update ran while paused");

            engine.stepOnce(0.05); // Step button MUST advance even while paused!
            if (engine.getTotalTicks() != tickBefore + 1) throw new AssertionError("stepOnce failed when paused");

            int countBefore = engine.getGrid().getAllEntities().size();
            engine.spawnEntity(Plant.createDefault(new Vector2D(300, 300)));
            if (engine.getGrid().getAllEntities().size() != countBefore + 1) {
                throw new AssertionError("spawnEntity did not synchronize immediately");
            }

            engine.triggerWildfire(new Vector2D(500, 350), 140.0);
            engine.triggerDrought(0.5);
            engine.triggerFertileBloom();
            engine.reset();
            if (engine.getTotalTicks() != 0 || !engine.isRunning()) {
                throw new AssertionError("Reset state mismatch");
            }
            System.out.println("PASS 10: Interactive controls (Pause, Step, Spawn, Disasters, Reset) verified.");
            passed++;
        } catch (Exception e) {
            System.err.println("FAIL Test 10: " + e.getMessage());
        }

        // Test 11: AdrenalineBuff Expiration & Unwrapping
        try {
            Engine engine = new Engine(SimulationConfig.createDefault());
            Herbivore herb = Herbivore.createDefault(new Vector2D(400, 300));
            AdrenalineBuff buff = new AdrenalineBuff(herb, 0.1);
            engine.spawnEntity(buff);
            for (int i = 0; i < 5; i++) {
                engine.update(0.05);
            }
            boolean stillWrapped = engine.getGrid().getAllEntities().stream()
                    .anyMatch(e -> e == buff);
            if (stillWrapped) throw new AssertionError("Expired AdrenalineBuff was not unwrapped");
            System.out.println("PASS 11: Temporary AdrenalineBuff expiration & auto-unwrap verified.");
            passed++;
        } catch (Exception e) {
            System.err.println("FAIL Test 11: " + e.getMessage());
        }

        // Test 12: Endurance 1500-Tick Ecological Stability Run
        try {
            Engine engine = new Engine(SimulationConfig.createDefault());
            for (int i = 0; i < 1500; i++) {
                engine.update(0.05);
            }
            EcosystemSummary summary = engine.computeParallelEcosystemMetrics();
            System.out.printf("PASS 12: 1500-tick endurance run completed cleanly. Active organisms: %d, H' = %.3f, Species: %s%n",
                    summary.totalOrganisms(), summary.shannonDiversityIndex(), summary.speciesCounts());
            if (summary.totalOrganisms() <= 0) throw new AssertionError("Total ecosystem extinction");
            passed++;
        } catch (Exception e) {
            System.err.println("FAIL Test 12: " + e.getMessage());
        }

        System.out.printf("%nResult: %d / %d Tests Passed Successfully.%n", passed, total);
        if (passed != total) {
            System.exit(1);
        }
    }
}
