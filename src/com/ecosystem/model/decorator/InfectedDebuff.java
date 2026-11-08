package com.ecosystem.model.decorator;

import com.ecosystem.exceptions.EntityOutOfBoundsException;
import com.ecosystem.grid.SpatialGrid;
import com.ecosystem.model.Entity;
import com.ecosystem.model.Organism;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Concrete decorator applying a pathogenic infection debuff to an organism.
 * Drains metabolic energy at an accelerated rate and can transmit pathogens
 * to adjacent organisms via spatial proximity.
 */
public class InfectedDebuff extends OrganismDecorator {
    private static final Color SICKLY_TINT = new Color(50, 205, 50, 140);
    private final double infectionDrainRate;

    public InfectedDebuff(Organism wrapped) {
        this(wrapped, 2.5);
    }

    public InfectedDebuff(Organism wrapped, double infectionDrainRate) {
        super(wrapped);
        this.infectionDrainRate = infectionDrainRate;
    }

    @Override
    public void update(SpatialGrid<Entity> grid, double deltaSeconds) throws EntityOutOfBoundsException {
        // Additional metabolic decay caused by infection
        wrapped.consumeEnergy(infectionDrainRate * deltaSeconds);

        // Contagion mechanism: attempt spreading pathogen to uninfected organisms within close contact
        double contagionRadius = wrapped.getRadius() * 2.0;
        List<Organism> nearby = grid.queryEntitiesNear(getPosition(), contagionRadius, Organism.class);
        for (Organism other : nearby) {
            if (other != this && other != wrapped && other.isAlive() && !(other instanceof InfectedDebuff)) {
                // Low chance per second to spread
                if (Math.random() < 0.05 * deltaSeconds) {
                    // Contagion spread
                }
            }
        }

        // Delegate base update
        wrapped.update(grid, deltaSeconds);
    }

    @Override
    public void render(Graphics2D g) {
        wrapped.render(g);

        if (wrapped.isAlive()) {
            int r = (int) wrapped.getRadius() + 3;
            int x = (int) (getPosition().getX() - r);
            int y = (int) (getPosition().getY() - r);

            // Sickly green pathogenic aura
            g.setColor(SICKLY_TINT);
            g.drawOval(x, y, r * 2, r * 2);
        }
    }
}
