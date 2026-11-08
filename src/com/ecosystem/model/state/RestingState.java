package com.ecosystem.model.state;

import com.ecosystem.core.Vector2D;
import com.ecosystem.exceptions.EntityOutOfBoundsException;
import com.ecosystem.grid.SpatialGrid;
import com.ecosystem.model.Entity;
import com.ecosystem.model.entities.Animal;

import java.awt.Color;
import java.util.Optional;

/**
 * State representing metabolic resting and physical recovery.
 * Halts kinetic motion to minimize energy expenditure when stamina is depleted.
 */
public class RestingState implements AnimalState {
    private static final Color STATUS_COLOR = new Color(160, 160, 160);
    private double restDuration = 0.0;

    @Override
    public void update(Animal animal, SpatialGrid<Entity> grid, double deltaSeconds)
            throws EntityOutOfBoundsException {
        restDuration += deltaSeconds;

        // Emergency threat response overrides rest
        Optional<? extends Entity> threat = animal.findThreat(grid);
        if (threat.isPresent()) {
            animal.changeState(new FleeingState(threat.get()));
            return;
        }

        // Halts movement: velocity dampened
        animal.setVelocity(new Vector2D(0, 0));
        animal.setSprinting(false);
        // Burn only fraction of metabolism during deep rest
        animal.burnBaseMetabolism(deltaSeconds * 0.3);

        // Check if recovered enough energy or rested 4 seconds
        if (animal.getEnergy() > animal.getMaxEnergy() * 0.35 || restDuration > 4.0) {
            animal.changeState(new WanderingState());
        }
    }

    @Override
    public String getStateName() {
        return "Resting";
    }

    @Override
    public Color getStatusColor() {
        return STATUS_COLOR;
    }
}

