package com.ecosystem.model.strategy;

import com.ecosystem.core.Vector2D;
import com.ecosystem.grid.SpatialGrid;
import com.ecosystem.model.Entity;
import com.ecosystem.model.entities.Animal;

/**
 * Strategy pattern functional interface encapsulating interchangeable steering
 * algorithms.
 * Allows runtime behavior swapping via concrete classes, lambda expressions, or
 * method references.
 */
@FunctionalInterface
public interface MovementStrategy {

    /**
     * Calculates the steering vector force applied to the animal context.
     *
     * @param animal       The animal executing the steering algorithm.
     * @param grid         The spatial grid for perceiving nearby neighbors and
     *                     obstacles.
     * @param deltaSeconds Time elapsed since previous update.
     * @return Resulting 2D steering force vector.
     */
    Vector2D calculateSteering(Animal animal, SpatialGrid<Entity> grid, double deltaSeconds);
}
