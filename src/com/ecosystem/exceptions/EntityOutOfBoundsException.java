package com.ecosystem.exceptions;

import com.ecosystem.core.Vector2D;

/**
 * Thrown when an entity attempts to move or spawn outside the bounded simulation grid.
 */
public class EntityOutOfBoundsException extends EcosystemException {
    private final Vector2D attemptedPosition;
    private final double maxX;
    private final double maxY;

    public EntityOutOfBoundsException(Vector2D attemptedPosition, double maxX, double maxY) {
        super(String.format("Entity position %s is out of grid bounds [0..%.1f, 0..%.1f]",
                attemptedPosition, maxX, maxY));
        this.attemptedPosition = attemptedPosition;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public Vector2D getAttemptedPosition() {
        return attemptedPosition;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMaxY() {
        return maxY;
    }
}
