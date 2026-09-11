package com.ecosystem.model.entities;

import com.ecosystem.core.Vector2D;
import com.ecosystem.exceptions.EntityOutOfBoundsException;
import com.ecosystem.exceptions.ResourceDepletedException;
import com.ecosystem.grid.SpatialGrid;
import com.ecosystem.model.Entity;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Non-living entity representing biological remains left by deceased animals.
 * Serves as nutritional energy for carnivores and decomposers.
 */
public class Carcass extends Entity {
    private double biomass;
    private final double maxBiomass;
    private double decayTimer;

    public Carcass(Vector2D position, double initialBiomass) {
        super(position, Math.max(3.0, Math.min(10.0, initialBiomass * 0.1)), new Color(139, 69, 19, 180));
        this.biomass = initialBiomass;
        this.maxBiomass = initialBiomass;
        this.decayTimer = 30.0; // Remains for 30 seconds if not eaten
    }

    public double getBiomass() {
        return biomass;
    }

    public double harvestBiomass(double amount) throws ResourceDepletedException {
        if (biomass <= 0) {
            throw new ResourceDepletedException("Carcass Biomass", amount);
        }
        double eaten = Math.min(amount, biomass);
        biomass -= eaten;
        setRadius(Math.max(2.0, (biomass / maxBiomass) * 8.0));
        if (biomass <= 0.1) {
            markDead();
        }
        return eaten;
    }

    @Override
    public void update(SpatialGrid<Entity> grid, double deltaSeconds) throws EntityOutOfBoundsException {
        decayTimer -= deltaSeconds;
        biomass -= deltaSeconds * 0.5; // Natural decay
        if (decayTimer <= 0 || biomass <= 0.1) {
            markDead();
        }
    }

    @Override
    public void render(Graphics2D g) {
        if (!isAlive()) return;
        int r = (int) getRadius();
        int x = (int) (getPosition().getX() - r);
        int y = (int) (getPosition().getY() - r);

        g.setColor(getColor());
        g.fillOval(x, y, r * 2, r * 2);
        g.setColor(new Color(60, 30, 10));
        g.drawOval(x, y, r * 2, r * 2);
    }
}
