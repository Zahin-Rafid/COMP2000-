package com.ecosystem.model.decorator;

import com.ecosystem.core.Vector2D;
import com.ecosystem.exceptions.EntityOutOfBoundsException;
import com.ecosystem.grid.SpatialGrid;
import com.ecosystem.model.Entity;
import com.ecosystem.model.Organism;
import com.ecosystem.model.genetics.Genome;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Structural Decorator pattern abstract class wrapping an Organism instance.
 * Allows dynamic behavioral enhancement (buffs, debuffs, status effects) at runtime
 * without mutating the underlying class hierarchy or causing subclass explosion.
 */
public abstract class OrganismDecorator extends Organism {
    protected final Organism wrapped;

    public OrganismDecorator(Organism wrapped) {
        super(
                wrapped.getPosition(),
                wrapped.getRadius(),
                wrapped.getColor(),
                wrapped.getEnergy(),
                wrapped.getMaxEnergy(),
                wrapped.getMaxAge(),
                wrapped.getReproductionThreshold(),
                wrapped.getGenome(),
                wrapped.getGeneration()
        );
        this.wrapped = wrapped;
    }

    public Organism getWrapped() {
        return wrapped;
    }

    /**
     * Traverses nested decorators to obtain the innermost concrete Organism.
     */
    public Organism unwrap() {
        Organism current = wrapped;
        while (current instanceof OrganismDecorator dec) {
            current = dec.getWrapped();
        }
        return current;
    }

    /**
     * Checks whether this decorator chain contains a specific decorator type.
     */
    public static boolean hasDecorator(Organism org, Class<? extends OrganismDecorator> decoratorClass) {
        Organism current = org;
        while (current instanceof OrganismDecorator dec) {
            if (decoratorClass.isInstance(dec)) {
                return true;
            }
            current = dec.getWrapped();
        }
        return false;
    }

    @Override
    public long getId() {
        return wrapped.getId();
    }

    @Override
    public Vector2D getPosition() {
        return wrapped.getPosition();
    }

    @Override
    public void setPosition(Vector2D position) {
        wrapped.setPosition(position);
    }

    @Override
    public boolean isAlive() {
        return wrapped.isAlive();
    }

    @Override
    public void setAlive(boolean alive) {
        wrapped.setAlive(alive);
    }

    @Override
    public void markDead() {
        wrapped.markDead();
    }

    @Override
    public double getRadius() {
        return wrapped.getRadius();
    }

    @Override
    public void setRadius(double radius) {
        wrapped.setRadius(radius);
    }

    @Override
    public Color getColor() {
        return wrapped.getColor();
    }

    @Override
    public void setColor(Color color) {
        wrapped.setColor(color);
    }

    @Override
    public double getEnergy() {
        return wrapped.getEnergy();
    }

    @Override
    public void setEnergy(double energy) {
        wrapped.setEnergy(energy);
    }

    @Override
    public void addEnergy(double amount) {
        wrapped.addEnergy(amount);
    }

    @Override
    public void consumeEnergy(double amount) {
        wrapped.consumeEnergy(amount);
    }

    @Override
    public double getMaxEnergy() {
        return wrapped.getMaxEnergy();
    }

    @Override
    public double getAge() {
        return wrapped.getAge();
    }

    @Override
    public double getMaxAge() {
        return wrapped.getMaxAge();
    }

    @Override
    public double getReproductionThreshold() {
        return wrapped.getReproductionThreshold();
    }

    @Override
    public int getGeneration() {
        return wrapped.getGeneration();
    }

    @Override
    public Genome getGenome() {
        return wrapped.getGenome();
    }

    @Override
    public String getSpeciesName() {
        return wrapped.getSpeciesName();
    }

    @Override
    public boolean canReproduce() {
        return wrapped.canReproduce();
    }

    @Override
    public void burnBaseMetabolism(double deltaSeconds) {
        wrapped.burnBaseMetabolism(deltaSeconds);
    }

    @Override
    public Organism reproduce(SpatialGrid<Entity> grid) throws EntityOutOfBoundsException {
        return wrapped.reproduce(grid);
    }

    @Override
    public void update(SpatialGrid<Entity> grid, double deltaSeconds) throws EntityOutOfBoundsException {
        wrapped.update(grid, deltaSeconds);
    }

    @Override
    public void render(Graphics2D g) {
        wrapped.render(g);
    }
}
