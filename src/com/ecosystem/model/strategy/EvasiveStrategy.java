package com.ecosystem.model.strategy;

import com.ecosystem.core.Vector2D;
import com.ecosystem.grid.SpatialGrid;
import com.ecosystem.model.Entity;
import com.ecosystem.model.entities.Animal;

import java.util.Optional;

/**
 * Defensive strategy computing an evasive steering force away from nearest predator threats.
 */
public class EvasiveStrategy implements MovementStrategy {
    private final WanderStrategy fallbackWander = new WanderStrategy();

    @Override
    public Vector2D calculateSteering(Animal animal, SpatialGrid<Entity> grid, double deltaSeconds) {
        Optional<? extends Entity> threat = animal.findThreat(grid);
        if (threat.isPresent()) {
            return animal.calculateFleeForce(threat.get().getPosition(), 1.4);
        }
        return fallbackWander.calculateSteering(animal, grid, deltaSeconds);
    }
}
