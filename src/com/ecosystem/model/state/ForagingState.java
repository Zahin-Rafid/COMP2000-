package com.ecosystem.model.state;

import com.ecosystem.core.Vector2D;
import com.ecosystem.exceptions.EntityOutOfBoundsException;
import com.ecosystem.grid.SpatialGrid;
import com.ecosystem.model.Entity;
import com.ecosystem.model.entities.Animal;

import java.awt.Color;
import java.util.Optional;

/**
 * State representing active searching and targeting of food sources (grazing or hunting).
 */
public class ForagingState implements AnimalState {
    private static final Color STATUS_COLOR = new Color(255, 200, 50);
    private Entity targetFood;

    public ForagingState(Entity targetFood) {
        this.targetFood = targetFood;
    }

    @Override
    public void update(Animal animal, SpatialGrid<Entity> grid, double deltaSeconds)
            throws EntityOutOfBoundsException {
        // Threat interrupt
        Optional<? extends Entity> threat = animal.findThreat(grid);
        if (threat.isPresent()) {
            animal.changeState(new FleeingState(threat.get()));
            return;
        }

        // Validate target existence
        if (targetFood == null || !targetFood.isAlive()) {
            Optional<? extends Entity> nextFood = animal.findFood(grid);
            if (nextFood.isPresent()) {
                targetFood = nextFood.get();
            } else {
                animal.changeState(new WanderingState());
                return;
            }
        }

        double dist = animal.getPosition().distanceTo(targetFood.getPosition());
        double contactRange = animal.getRadius() + targetFood.getRadius() + 3.0;

        if (dist <= contactRange) {
            // Feeding action
            animal.consumeTarget(targetFood, grid);
            // Satiated or check reproduction
            if (animal.canReproduce()) {
                Optional<? extends Animal> mate = animal.findMate(grid);
                if (mate.isPresent()) {
                    animal.changeState(new MatingState(mate.get()));
                    return;
                }
            }
            animal.changeState(new WanderingState());
        } else {
            // Steer towards food
            animal.setSprinting(dist < animal.getGenome().getVisionRadius() * 0.5);
            Vector2D steer = animal.calculateSteeringForce(targetFood.getPosition(), 1.15);
            animal.applyKineticMovement(grid, steer, deltaSeconds);
        }
    }

    @Override
    public String getStateName() {
        return "Foraging";
    }

    @Override
    public Color getStatusColor() {
        return STATUS_COLOR;
    }
}
