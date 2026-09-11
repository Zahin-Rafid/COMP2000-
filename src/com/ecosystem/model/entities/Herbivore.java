package com.ecosystem.model.entities;

import com.ecosystem.core.Vector2D;
import com.ecosystem.exceptions.EntityOutOfBoundsException;
import com.ecosystem.exceptions.InvalidGenomeException;
import com.ecosystem.grid.SpatialGrid;
import com.ecosystem.model.Entity;
import com.ecosystem.model.genetics.Genome;

import java.awt.Color;
import java.util.List;
import java.util.Optional;

/**
 * Primary consumer (herbivore) that grazes on plants and evades predators.
 */
public class Herbivore extends Animal {
    private static final Color HERBIVORE_COLOR = new Color(50, 150, 250);

    public Herbivore(Vector2D position, double initialEnergy, Genome genome, int generation) {
        super(
                position,
                Math.max(4.0, genome.getSize()),
                HERBIVORE_COLOR,
                initialEnergy,
                120.0,
                70.0,  // Max age
                75.0,  // Reproduction threshold
                genome,
                generation
        );
    }

    public static Herbivore createDefault(Vector2D position) {
        try {
            Genome defaultGenome = new Genome(65.0, 90.0, 5.0, 1.0, 0.08);
            return new Herbivore(position, 50.0, defaultGenome, 1);
        } catch (InvalidGenomeException e) {
            throw new RuntimeException("Default herbivore genome initialization failed", e);
        }
    }

    @Override
    public void update(SpatialGrid<Entity> grid, double deltaSeconds) throws EntityOutOfBoundsException {
        if (!isAlive()) return;

        double vision = getGenome().getVisionRadius();

        // 1. Check for immediate predator threats (Carnivore or ApexPredator)
        Optional<Carnivore> threatCarnivore = grid.findNearest(getPosition(), vision * 0.9, Carnivore.class, Entity::isAlive);
        Optional<ApexPredator> threatApex = grid.findNearest(getPosition(), vision * 1.1, ApexPredator.class, Entity::isAlive);

        Vector2D steeringForce;
        if (threatApex.isPresent()) {
            setSprinting(true);
            steeringForce = calculateFleeForce(threatApex.get().getPosition(), 1.4);
        } else if (threatCarnivore.isPresent()) {
            setSprinting(true);
            steeringForce = calculateFleeForce(threatCarnivore.get().getPosition(), 1.3);
        } else {
            setSprinting(false);
            // 2. Look for Food (Plant)
            Optional<Plant> nearestPlant = grid.findNearest(getPosition(), vision, Plant.class, Entity::isAlive);

            if (nearestPlant.isPresent()) {
                Plant food = nearestPlant.get();
                double dist = getPosition().distanceTo(food.getPosition());
                if (dist <= getRadius() + food.getRadius() + 2.0) {
                    // Consume plant
                    addEnergy(food.getEnergy() * 0.8);
                    food.markDead();
                    steeringForce = calculateWanderForce();
                } else {
                    steeringForce = calculateSteeringForce(food.getPosition(), 1.0);
                }
            } else {
                // 3. Herd with nearby herbivores
                List<Herbivore> flock = grid.queryEntitiesNear(getPosition(), vision * 0.5, Herbivore.class);
                if (flock.size() > 1) {
                    Vector2D flockCenter = new Vector2D(0, 0);
                    int count = 0;
                    for (Herbivore mate : flock) {
                        if (mate != this) {
                            flockCenter = flockCenter.add(mate.getPosition());
                            count++;
                        }
                    }
                    flockCenter = flockCenter.multiply(1.0 / count);
                    steeringForce = calculateSteeringForce(flockCenter, 0.6).add(calculateWanderForce().multiply(0.4));
                } else {
                    steeringForce = calculateWanderForce();
                }
            }
        }

        applyKineticMovement(grid, steeringForce, deltaSeconds);

        // Check reproduction
        if (canReproduce()) {
            Herbivore baby = reproduce(grid);
            if (baby != null) {
                grid.addEntity(baby);
                consumeEnergy(getReproductionThreshold() * 0.45);
            }
        }
    }

    @Override
    protected void onDeath() {
        // Drop small carcass
    }

    @Override
    public Herbivore reproduce(SpatialGrid<Entity> grid) {
        try {
            double offsetAngle = RNG.nextDouble() * 2 * Math.PI;
            Vector2D babyPos = getPosition().add(new Vector2D(Math.cos(offsetAngle) * 10, Math.sin(offsetAngle) * 10));
            if (!grid.isWithinBounds(babyPos)) {
                babyPos = getPosition();
            }
            Genome childGenome = getGenome().mutate();
            return new Herbivore(babyPos, 35.0, childGenome, getGeneration() + 1);
        } catch (InvalidGenomeException e) {
            return null;
        }
    }
}
