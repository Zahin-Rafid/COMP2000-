package com.ecosystem.model.strategy;

import com.ecosystem.core.Vector2D;
import com.ecosystem.grid.SpatialGrid;
import com.ecosystem.model.Entity;
import com.ecosystem.model.entities.Animal;

import java.util.Random;

/**
 * Concrete movement strategy implementing Reynolds' smooth circle-projection wander algorithm.
 */
public class WanderStrategy implements MovementStrategy {
    private static final Random RNG = new Random();
    private double wanderAngle;

    public WanderStrategy() {
        this.wanderAngle = RNG.nextDouble() * 2 * Math.PI;
    }

    @Override
    public Vector2D calculateSteering(Animal animal, SpatialGrid<Entity> grid, double deltaSeconds) {
        wanderAngle += (RNG.nextDouble() - 0.5) * 0.8;
        double wanderRadius = 15.0;
        double circleDist = 25.0;

        double heading = animal.getHeadingAngle();
        Vector2D circleCenter = new Vector2D(Math.cos(heading), Math.sin(heading)).multiply(circleDist);
        Vector2D displacement = new Vector2D(Math.cos(wanderAngle), Math.sin(wanderAngle)).multiply(wanderRadius);
        return circleCenter.add(displacement);
    }
}
