package com.ecosystem.model.decorator;

import com.ecosystem.exceptions.EntityOutOfBoundsException;
import com.ecosystem.grid.SpatialGrid;
import com.ecosystem.model.Entity;
import com.ecosystem.model.Organism;
import com.ecosystem.model.entities.Animal;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

/**
 * Concrete decorator applying a temporary adrenaline surge to an organism.
 * Boosts sprint speed and recovers stamina briefly before expiring.
 */
public class AdrenalineBuff extends OrganismDecorator {
    private static final Color ADRENALINE_AURA = new Color(255, 140, 0, 200);
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
        if (!isExpired() && unwrap() instanceof Animal animal) {
            animal.setSprinting(true);
            // Adrenaline provides a small extra burst forward along heading
            wrapped.update(grid, deltaSeconds * 1.35);
        } else {
            wrapped.update(grid, deltaSeconds);
        }
    }

    @Override
    public void render(Graphics2D g) {
        wrapped.render(g);

        if (wrapped.isAlive() && !isExpired()) {
            int r = (int) wrapped.getRadius() + 4;
            int x = (int) (getPosition().getX() - r);
            int y = (int) (getPosition().getY() - r);

            Stroke prevStroke = g.getStroke();
            g.setColor(ADRENALINE_AURA);
            g.setStroke(new BasicStroke(2.0f));
            g.drawOval(x, y, r * 2, r * 2);
            g.setStroke(prevStroke);
        }
    }
}
