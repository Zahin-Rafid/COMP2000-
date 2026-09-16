package com.ecosystem.model.entities;

import com.ecosystem.core.Vector2D;
import com.ecosystem.exceptions.EntityOutOfBoundsException;
import com.ecosystem.exceptions.InvalidGenomeException;
import com.ecosystem.grid.SpatialGrid;
import com.ecosystem.model.Entity;
import com.ecosystem.model.genetics.Genome;

import java.awt.Color;
import java.util.Optional;

/**
 * Tertiary apex predator at the top of the trophic pyramid.
 * Hunts both herbivores and carnivores with large territorial dominance.
 */
public class ApexPredator extends Animal {
    private static final Color APEX_COLOR = new Color(140, 20, 180);

    public ApexPredator(Vector2D position, double initialEnergy, Genome genome, int generation) {
        super(
                position,
                Math.max(9.0, genome.getSize() * 1.6),
                APEX_COLOR,
                initialEnergy,
                220.0,
                110.0, // Max age
                140.0, // Reproduction threshold
                genome,
                generation);
    }

    public static ApexPredator createDefault(Vector2D position) {
        try {
            Genome defaultGenome = new Genome(75.0, 160.0, 9.0, 1.4, 0.04);
            return new ApexPredator(position, 100.0, defaultGenome, 1);
        } catch (InvalidGenomeException e) {
            throw new RuntimeException("Default apex predator genome initialization failed", e);
        }
    }

    @Override
    public void update(SpatialGrid<Entity> grid, double deltaSeconds) throws EntityOutOfBoundsException {
        if (!isAlive())
            return;

        double vision = getGenome().getVisionRadius();

        // Apex predators prioritize carnivores for higher caloric return, then
        // herbivores
        Optional<Carnivore> targetCarnivore = grid.findNearest(getPosition(), vision, Carnivore.class, Entity::isAlive);
        Optional<Herbivore> targetHerbivore = grid.findNearest(getPosition(), vision, Herbivore.class, Entity::isAlive);

        Vector2D steeringForce;
        if (targetCarnivore.isPresent()) {
            setSprinting(true);
            Carnivore target = targetCarnivore.get();
            double dist = getPosition().distanceTo(target.getPosition());
            if (dist <= getRadius() + target.getRadius() + 4.0) {
                addEnergy(target.getEnergy() * 0.8 + 40.0);
                target.markDead();
                try {
                    grid.addEntity(new Carcass(target.getPosition(), 25.0));
                } catch (EntityOutOfBoundsException ignored) {
                }
                steeringForce = calculateWanderForce();
            } else {
                steeringForce = calculateSteeringForce(target.getPosition(), 1.2);
            }
        } else if (targetHerbivore.isPresent()) {
            setSprinting(true);
            Herbivore target = targetHerbivore.get();
            double dist = getPosition().distanceTo(target.getPosition());
            if (dist <= getRadius() + target.getRadius() + 4.0) {
                addEnergy(target.getEnergy() * 0.8 + 20.0);
                target.markDead();
                try {
                    grid.addEntity(new Carcass(target.getPosition(), 15.0));
                } catch (EntityOutOfBoundsException ignored) {
                }
                steeringForce = calculateWanderForce();
            } else {
                steeringForce = calculateSteeringForce(target.getPosition(), 1.1);
            }
        } else {
            setSprinting(false);
            steeringForce = calculateWanderForce();
        }

        applyKineticMovement(grid, steeringForce, deltaSeconds);

        if (canReproduce()) {
            ApexPredator cub = reproduce(grid);
            if (cub != null) {
                grid.addEntity(cub);
                consumeEnergy(getReproductionThreshold() * 0.6);
            }
        }
    }

    @Override
    public ApexPredator reproduce(SpatialGrid<Entity> grid) {
        try {
            double offsetAngle = RNG.nextDouble() * 2 * Math.PI;
            Vector2D babyPos = getPosition().add(new Vector2D(Math.cos(offsetAngle) * 15, Math.sin(offsetAngle) * 15));
            if (!grid.isWithinBounds(babyPos)) {
                babyPos = getPosition();
            }
            Genome childGenome = getGenome().mutate();
            return new ApexPredator(babyPos, 70.0, childGenome, getGeneration() + 1);
        } catch (InvalidGenomeException e) {
            return null;
        }
    }
}
