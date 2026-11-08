package com.ecosystem.model.decorator;

import com.ecosystem.exceptions.EntityOutOfBoundsException;
import com.ecosystem.grid.SpatialGrid;
import com.ecosystem.model.Entity;
import com.ecosystem.model.Organism;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Concrete decorator applying a temporary adrenaline surge to an organism.
 * Boosts metabolic reaction speed and provides resistance to exhaustion.
 */
public class AdrenalineBuff extends OrganismDecorator {
    private static final Color ADRENALINE_AURA = new Color(255, 140, 0, 160);
    private double remainingDuration;

    public AdrenalineBuff(Organism wrapped, double durationSeconds) {
        super(wrapped);
        this.remainingDuration = durationSeconds;
    }

    public boolean isExpired() {
        return remainingDuration <= 0.0;
    }

    @Override
    public void update(SpatialGrid<Entity> grid, double deltaSeconds) throws EntityOutOfBoundsException {
        remainingDuration -= deltaSeconds;
        wrapped.update(grid, deltaSeconds);
    }

    @Override
    public void render(Graphics2D g) {
        wrapped.render(g);

        if (wrapped.isAlive() && !isExpired()) {
            int r = (int) wrapped.getRadius() + 3;
            int x = (int) (getPosition().getX() - r);
            int y = (int) (getPosition().getY() - r);

            // Fiery adrenaline corona
            g.setColor(ADRENALINE_AURA);
            g.drawOval(x, y, r * 2, r * 2);
        }
    }
}
