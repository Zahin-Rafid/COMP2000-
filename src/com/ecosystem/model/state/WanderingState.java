package com.ecosystem.model.state;

import com.ecosystem.core.Vector2D;
import com.ecosystem.exceptions.EntityOutOfBoundsException;
import com.ecosystem.grid.SpatialGrid;
import com.ecosystem.model.Entity;
import com.ecosystem.model.Organism;
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

        // 2. Evaluate hunger before anything else if energy < 75% (Transition -> ForagingState)
        if (animal.getEnergy() < animal.getMaxEnergy() * 0.75) {
            Optional<? extends Entity> food = animal.findFood(grid);
            if (food.isPresent()) {
                animal.changeState(new ForagingState(food.get()));
                return;
            }
        }

        // 3. Evaluate reproduction viability (Transition -> MatingState)
        if (animal.canReproduce()) {
            Optional<? extends Animal> mate = animal.findMate(grid);
            if (mate.isPresent()) {
                animal.changeState(new MatingState(mate.get()));
                return;
            }
            // Ecological resilience: if well above surplus energy and isolated, allow solitary spawn
            if (animal.getEnergy() >= animal.getMaxEnergy() * 0.82) {
                Organism offspring = animal.reproduce(grid);
                if (offspring != null) {
                    double pad = offspring.getRadius() + 4.0;
                    offspring.setPosition(offspring.getPosition().clamp(pad, pad,
                            grid.getWorldWidth() - pad, grid.getWorldHeight() - pad));
                    grid.addEntity(offspring);
                    animal.consumeEnergy(animal.getReproductionThreshold() * 0.45);
                }
            }
        }

        // 4. Default wandering movement using active MovementStrategy
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
