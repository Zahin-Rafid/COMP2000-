package com.ecosystem.model.state;

import com.ecosystem.exceptions.EntityOutOfBoundsException;
import com.ecosystem.grid.SpatialGrid;
import com.ecosystem.model.Entity;
import com.ecosystem.model.entities.Animal;

import java.awt.Color;

/**
 * State pattern interface defining polymorphic behavioral states for animals.
 * Encapsulates mode-specific steering, sensing, metabolism, and state transition logic.
 */
public interface AnimalState {

    /**
     * Executes state-specific update behavior, steering force calculation,
     * and evaluates transition conditions to other states.
     *
     * @param animal The animal context whose state is being updated.
     * @param grid The spatial world grid containing other entities.
     * @param deltaSeconds Elapsed simulation time in seconds.
     * @throws EntityOutOfBoundsException if position calculation violates world bounds.
     */
    void update(Animal animal, SpatialGrid<Entity> grid, double deltaSeconds)
            throws EntityOutOfBoundsException;

    /**
     * Human-readable state name (e.g., "Wandering", "Foraging", "Fleeing", "Mating", "Resting").
     */
    String getStateName();

    /**
     * Diagnostic HUD color indicator associated with this state.
     */
    Color getStatusColor();
}
