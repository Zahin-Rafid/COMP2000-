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
    public void markDead() {
        wrapped.markDead();
    }

    @Override
    public double getEnergy() {
        return wrapped.getEnergy();
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
    public double getAge() {
        return wrapped.getAge();
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
    public boolean canReproduce() {
        return wrapped.canReproduce();
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
