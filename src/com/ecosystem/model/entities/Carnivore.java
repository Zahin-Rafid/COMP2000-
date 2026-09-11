package com.ecosystem.model.entities;

import com.ecosystem.core.Vector2D;
import com.ecosystem.exceptions.EntityOutOfBoundsException;
import com.ecosystem.exceptions.InvalidGenomeException;
import com.ecosystem.exceptions.ResourceDepletedException;
import com.ecosystem.grid.SpatialGrid;
import com.ecosystem.model.Entity;
import com.ecosystem.model.genetics.Genome;

import java.awt.Color;
import java.util.Optional;

/**
 * Secondary consumer (carnivore/predator) that hunts herbivores and avoids apex predators.
 */
public class Carnivore extends Animal {
    private static final Color CARNIVORE_COLOR = new Color(220, 50, 50);

    public Carnivore(Vector2D position, double initialEnergy, Genome genome, int generation) {
        super(
                position,
                Math.max(6.0, genome.getSize() * 1.2),
                CARNIVORE_COLOR,
                initialEnergy,
                150.0,
                90.0,  // Max age
                95.0,  // Reproduction threshold
                genome,
                generation
        );
    }

    public static Carnivore createDefault(Vector2D position) {
        try {
            Genome defaultGenome = new Genome(85.0, 130.0, 6.5, 1.2, 0.06);
            return new Carnivore(position, 60.0, defaultGenome, 1);
        } catch (InvalidGenomeException e) {
            throw new RuntimeException("Default carnivore genome initialization failed", e);
        }
    }

    @Override
    public void update(SpatialGrid<Entity> grid, double deltaSeconds) throws EntityOutOfBoundsException {
        if (!isAlive()) return;

        double vision = getGenome().getVisionRadius();

        // 1. Check for Apex Predator threats
        Optional<ApexPredator> threat = grid.findNearest(getPosition(), vision * 0.8, ApexPredator.class, Entity::isAlive);

        Vector2D steeringForce;
        if (threat.isPresent()) {
            setSprinting(true);
            steeringForce = calculateFleeForce(threat.get().getPosition(), 1.4);
        } else {
            // 2. Look for Herbivores to hunt
            Optional<Herbivore> prey = grid.findNearest(getPosition(), vision, Herbivore.class, Entity::isAlive);

            if (prey.isPresent()) {
                setSprinting(true);
                Herbivore target = prey.get();
                double dist = getPosition().distanceTo(target.getPosition());
                if (dist <= getRadius() + target.getRadius() + 3.0) {
                    // Catch and consume prey
                    addEnergy(target.getEnergy() * 0.9 + 20.0);
                    target.markDead();
                    // Spawn carcass remains
                    try {
                        grid.addEntity(new Carcass(target.getPosition(), 15.0));
                    } catch (EntityOutOfBoundsException ignored) {}
                    steeringForce = calculateWanderForce();
                } else {
                    steeringForce = calculateSteeringForce(target.getPosition(), 1.3);
                }
            } else {
                setSprinting(false);
                // 3. Scavenge nearby Carcass if hungry
                Optional<Carcass> carcass = grid.findNearest(getPosition(), vision * 0.7, Carcass.class, Entity::isAlive);
                if (carcass.isPresent() && getEnergy() < getMaxEnergy() * 0.7) {
                    Carcass food = carcass.get();
                    double dist = getPosition().distanceTo(food.getPosition());
                    if (dist <= getRadius() + food.getRadius() + 2.0) {
                        try {
                            double harvested = food.harvestBiomass(25.0 * deltaSeconds);
                            addEnergy(harvested * 2.0);
                        } catch (ResourceDepletedException e) {
                            // Carcass empty
                        }
                        steeringForce = calculateWanderForce();
                    } else {
                        steeringForce = calculateSteeringForce(food.getPosition(), 0.9);
                    }
                } else {
                    steeringForce = calculateWanderForce();
                }
            }
        }

        applyKineticMovement(grid, steeringForce, deltaSeconds);

        if (canReproduce()) {
            Carnivore pup = reproduce(grid);
            if (pup != null) {
                grid.addEntity(pup);
                consumeEnergy(getReproductionThreshold() * 0.5);
            }
        }
    }

    @Override
    public Carnivore reproduce(SpatialGrid<Entity> grid) {
        try {
            double offsetAngle = RNG.nextDouble() * 2 * Math.PI;
            Vector2D babyPos = getPosition().add(new Vector2D(Math.cos(offsetAngle) * 12, Math.sin(offsetAngle) * 12));
            if (!grid.isWithinBounds(babyPos)) {
                babyPos = getPosition();
            }
            Genome childGenome = getGenome().mutate();
            return new Carnivore(babyPos, 45.0, childGenome, getGeneration() + 1);
        } catch (InvalidGenomeException e) {
            return null;
        }
    }
}
