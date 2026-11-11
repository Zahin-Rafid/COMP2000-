package com.ecosystem.model.state;

import com.ecosystem.core.Vector2D;
import com.ecosystem.exceptions.EntityOutOfBoundsException;
import com.ecosystem.grid.SpatialGrid;
import com.ecosystem.model.Entity;
import com.ecosystem.model.entities.Animal;

import java.awt.Color;
import java.util.Optional;

/**
 * State representing pursuit of a compatible mate for genetic reproduction.
 */
public class MatingState implements AnimalState {
    private static final Color STATUS_COLOR = new Color(255, 105, 180);
    private Animal mate;

    public MatingState(Animal mate) {
        this.mate = mate;
    }

    @Override
    public void update(Animal animal, SpatialGrid<Entity> grid, double deltaSeconds)
            throws EntityOutOfBoundsException {
        // Immediate threat interruption
        Optional<? extends Entity> threat = animal.findThreat(grid);
        if (threat.isPresent()) {
            animal.changeState(new FleeingState(threat.get()));
            return;
        }

        // Validate mate viability
        if (mate == null || !mate.isAlive() || !mate.canReproduce() || !animal.canReproduce()) {
            animal.changeState(new WanderingState());
            return;
        }

        double dist = animal.getPosition().distanceTo(mate.getPosition());
        double matingRange = animal.getRadius() + mate.getRadius() + 4.0;

        if (dist <= matingRange) {
            // Reproduce and safely clamp newborn inside world bounds
            Animal offspring = animal.breed(mate);
            if (offspring != null) {
                double pad = offspring.getRadius() + 4.0;
                Vector2D safePos = offspring.getPosition().clamp(pad, pad,
                        grid.getWorldWidth() - pad, grid.getWorldHeight() - pad);
                offspring.setPosition(safePos);
                grid.addEntity(offspring);
                animal.consumeEnergy(animal.getReproductionThreshold() * 0.45);
                mate.consumeEnergy(mate.getReproductionThreshold() * 0.45);
            }
            animal.changeState(new WanderingState());
            mate.changeState(new WanderingState());
        } else {
            // Steer towards mate
            animal.setSprinting(false);
            Vector2D steer = animal.calculateSteeringForce(mate.getPosition(), 1.0);
            animal.applyKineticMovement(grid, steer, deltaSeconds);
        }
    }

    @Override
    public String getStateName() {
        return "Mating";
    }

    @Override
    public Color getStatusColor() {
        return STATUS_COLOR;
    }
}
