package com.ecosystem.model.strategy;

import com.ecosystem.core.Vector2D;
import com.ecosystem.grid.SpatialGrid;
import com.ecosystem.model.Entity;
import com.ecosystem.model.entities.Animal;

import java.util.List;

/**
 * Concrete movement strategy implementing Reynolds' Boids flocking rules:
 * Cohesion (attraction to local flock center), Alignment (matching flock heading),
 * and Separation (repelling to avoid crowding).
 */
public class FlockingStrategy implements MovementStrategy {
    private final double perceptionRadius;
    private final WanderStrategy fallbackWander = new WanderStrategy();

    public FlockingStrategy(double perceptionRadius) {
        this.perceptionRadius = perceptionRadius;
    }

    @Override
    public Vector2D calculateSteering(Animal animal, SpatialGrid<Entity> grid, double deltaSeconds) {
        // Query conspecifics (same species) in sensory range using Streams API
        List<? extends Animal> neighbors = grid.queryEntitiesNear(animal.getPosition(), perceptionRadius, animal.getClass())
                .stream()
                .filter(other -> other != animal && other.isAlive())
                .toList();

        if (neighbors.isEmpty()) {
            return fallbackWander.calculateSteering(animal, grid, deltaSeconds);
        }

        Vector2D cohesionVector = new Vector2D(0, 0);
        Vector2D alignmentVector = new Vector2D(0, 0);
        Vector2D separationVector = new Vector2D(0, 0);

        for (Animal mate : neighbors) {
            cohesionVector = cohesionVector.add(mate.getPosition());
            alignmentVector = alignmentVector.add(mate.getVelocity());

            double dist = animal.getPosition().distanceTo(mate.getPosition());
            if (dist < animal.getRadius() * 2.5 && dist > 0.001) {
                Vector2D diff = animal.getPosition().subtract(mate.getPosition()).normalize().multiply(1.0 / dist);
                separationVector = separationVector.add(diff);
            }
        }

        // Compute average center of mass (cohesion)
        Vector2D center = cohesionVector.multiply(1.0 / neighbors.size());
        Vector2D steerCohesion = animal.calculateSteeringForce(center, 0.7);

        // Alignment force
        Vector2D steerAlignment = alignmentVector.multiply(1.0 / neighbors.size()).subtract(animal.getVelocity()).multiply(0.5);

        // Separation force
        Vector2D steerSeparation = separationVector.multiply(1.5);

        // Weighted blend of Boids rules + slight organic wander
        return steerCohesion.multiply(0.4)
                .add(steerAlignment.multiply(0.3))
                .add(steerSeparation.multiply(0.6))
                .add(fallbackWander.calculateSteering(animal, grid, deltaSeconds).multiply(0.2));
    }
}
