package com.ecosystem.model.state;

import com.ecosystem.core.Vector2D;
import com.ecosystem.exceptions.EntityOutOfBoundsException;
import com.ecosystem.grid.SpatialGrid;
import com.ecosystem.model.Entity;
import com.ecosystem.model.entities.Animal;

import java.awt.Color;
import java.util.Optional;

/**
 * State representing relaxed wandering behavior when no imminent danger or extreme hunger is present.
 */
public class WanderingState implements AnimalState {
    private static final Color STATUS_COLOR = new Color(100, 200, 255);

    @Override
    public void update(Animal animal, SpatialGrid<Entity> grid, double deltaSeconds)
            throws EntityOutOfBoundsException {
        // 1. Evaluate immediate survival threats (Transition -> FleeingState)
        Optional<? extends Entity> threat = animal.findThreat(grid);
        if (threat.isPresent()) {
            animal.changeState(new FleeingState(threat.get()));
            return;
        }

        // 2. Evaluate critical exhaustion (Transition -> RestingState)
        if (animal.getEnergy() < animal.getMaxEnergy() * 0.15) {
            animal.changeState(new RestingState());
            return;
        }

        // 3. Evaluate reproduction viability (Transition -> MatingState)
        if (animal.canReproduce()) {
            Optional<? extends Animal> mate = animal.findMate(grid);
            if (mate.isPresent()) {
                animal.changeState(new MatingState(mate.get()));
                return;
            }
        }

        // 4. Evaluate hunger (Transition -> ForagingState)
        if (animal.getEnergy() < animal.getMaxEnergy() * 0.65) {
            Optional<? extends Entity> food = animal.findFood(grid);
            if (food.isPresent()) {
                animal.changeState(new ForagingState(food.get()));
                return;
            }
        }

        // 5. Default wandering movement using active MovementStrategy
        animal.setSprinting(false);
        Vector2D steeringForce = animal.getMovementStrategy().calculateSteering(animal, grid, deltaSeconds);
        animal.applyKineticMovement(grid, steeringForce, deltaSeconds);
    }

    @Override
    public String getStateName() {
        return "Wandering";
    }

    @Override
    public Color getStatusColor() {
        return STATUS_COLOR;
    }
}

