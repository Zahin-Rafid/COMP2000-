package com.ecosystem.model;

import com.ecosystem.core.Vector2D;
import com.ecosystem.exceptions.EntityOutOfBoundsException;
import com.ecosystem.grid.SpatialGrid;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Abstract root of the simulation entity hierarchy.
 * Encapsulates unique identity, position, life state, and rendering contracts.
 */
public abstract class Entity {
    private static final AtomicLong ID_GENERATOR = new AtomicLong(1);

    private final long id;
    private Vector2D position;
    private boolean alive;
    private double radius;
    private Color color;

    public Entity(Vector2D position, double radius, Color color) {
        this.id = ID_GENERATOR.getAndIncrement();
        this.position = position;
        this.radius = radius;
        this.color = color;
        this.alive = true;
    }

    public long getId() {
        return id;
    }

    public Vector2D getPosition() {
        return position;
    }

    public void setPosition(Vector2D position) {
        this.position = position;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void markDead() {
        this.alive = false;
        onDeath();
    }

    /**
     * Hook method invoked upon entity mortality.
     */
    protected void onDeath() {
        // Subclasses may override to spawn carcasses, seeds, or nutrients.
    }

    /**
     * Abstract update step executed on each simulation tick.
     *
     * @param grid The spatial world grid containing environmental state.
     * @param deltaSeconds Time step elapsed in seconds.
     * @throws EntityOutOfBoundsException When movement leads to an invalid coordinate.
     */
    public abstract void update(SpatialGrid<Entity> grid, double deltaSeconds) throws EntityOutOfBoundsException;

    /**
     * Abstract render contract for drawing the entity on the Swing canvas.
     *
     * @param g Java2D graphics context.
     */
    public abstract void render(Graphics2D g);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return id == entity.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return String.format("%s#%d at %s", getClass().getSimpleName(), id, position);
    }
}
