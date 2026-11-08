package com.ecosystem.model.strategy;

import com.ecosystem.core.Vector2D;
import com.ecosystem.grid.BiomeType;
import com.ecosystem.grid.Cell;
import com.ecosystem.grid.SpatialGrid;
import com.ecosystem.model.Entity;
import com.ecosystem.model.entities.Animal;

/**
 * Predatory strategy prioritizing cover in dense vegetation (Forest biome)
 * to ambush unsuspecting prey while minimizing kinetic energy drain.
 */
public class AmbushStrategy implements MovementStrategy {
    private final WanderStrategy fallbackWander = new WanderStrategy();

    @Override
    public Vector2D calculateSteering(Animal animal, SpatialGrid<Entity> grid, double deltaSeconds) {
        Cell currentCell = grid.getCellAtWorldCoords(animal.getPosition());

        // If already concealed in forest cover, slow down dramatically to wait
        if (currentCell != null && currentCell.getBiome() == BiomeType.FOREST) {
            return animal.getVelocity().multiply(-0.8); // Deceleration force
        }

        // Otherwise steer towards the nearest forest patch
        Vector2D bestCover = null;
        double minDistance = Double.MAX_VALUE;

        for (int c = 0; c < grid.getCols(); c += 2) {
            for (int r = 0; r < grid.getRows(); r += 2) {
                Cell cell = grid.getCellAtGridCoords(c, r);
                if (cell != null && cell.getBiome() == BiomeType.FOREST) {
                    Vector2D center = new Vector2D((c + 0.5) * grid.getCellSize(), (r + 0.5) * grid.getCellSize());
                    double dist = animal.getPosition().distanceTo(center);
                    if (dist < minDistance) {
                        minDistance = dist;
                        bestCover = center;
                    }
                }
            }
        }

        if (bestCover != null && minDistance < animal.getGenome().getVisionRadius() * 1.5) {
            return animal.calculateSteeringForce(bestCover, 0.7);
        }

        return fallbackWander.calculateSteering(animal, grid, deltaSeconds);
    }
}
