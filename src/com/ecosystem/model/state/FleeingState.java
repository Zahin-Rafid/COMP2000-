package com.ecosystem.model.state;

import com.ecosystem.core.Vector2D;
import com.ecosystem.exceptions.EntityOutOfBoundsException;
import com.ecosystem.grid.SpatialGrid;
import com.ecosystem.model.Entity;
import com.ecosystem.model.entities.Animal;

import java.awt.Color;
import java.util.Optional;

/**
 * State representing high-intensity evasive sprint to escape predators.
 * Consumes elevated kinetic energy while maximizing survival chance.
 */
public class FleeingState implements AnimalState {
    private static final Color STATUS_COLOR = new Color(255, 60, 60);
    private Entity threat;

    public FleeingState(Entity threat) {
        this.threat = threat;
    }

    @Override
    public void update(Animal animal, SpatialGrid<Entity> grid, double deltaSeconds)
            throws EntityOutOfBoundsException {
        // Validate threat condition
        if (threat == null || !threat.isAlive()) {
            Optional<? extends Entity> otherThreat = animal.findThreat(grid);
            if (otherThreat.isPresent()) {
                threat = otherThreat.get();
            } else {
                animal.changeState(new WanderingState());
                return;
            }
        }

        double dist = animal.getPosition().distanceTo(threat.getPosition());
        double safeDistance = animal.getGenome().getVisionRadius() * 1.3;

        if (dist > safeDistance) {
            // Escaped threat
            animal.setSprinting(false);
            animal.changeState(new WanderingState());
            return;
        }

        if (animal.getEnergy() < animal.getMaxEnergy() * 0.08) {
            // Collapsed from exhaustion
            animal.changeState(new RestingState());
            return;
        }

        // Flee sprint
        animal.setSprinting(true);
        Vector2D fleeForce = animal.calculateFleeForce(threat.getPosition(), 1.45);
        animal.applyKineticMovement(grid, fleeForce, deltaSeconds);
    }

    @Override
    public String getStateName() {
        return "Fleeing";
    }

    @Override
    public Color getStatusColor() {
        return STATUS_COLOR;
    }
}

