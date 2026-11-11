package com.ecosystem.model.decorator;

import com.ecosystem.exceptions.EntityOutOfBoundsException;
import com.ecosystem.grid.SpatialGrid;
import com.ecosystem.model.Entity;
import com.ecosystem.model.Organism;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

/**
 * Concrete decorator applying environmental camouflage to an organism.
 * Reduces perceptual visibility range for approaching predators by {@code concealmentFactor}.
 */
public class CamouflageBuff extends OrganismDecorator {
    private static final Color CAMO_TINT = new Color(100, 230, 140, 170);
    private final double concealmentFactor;

    public CamouflageBuff(Organism wrapped) {
        this(wrapped, 0.5); // 50% detection radius reduction
    }

    public CamouflageBuff(Organism wrapped, double concealmentFactor) {
        super(wrapped);
        this.concealmentFactor = Math.max(0.0, Math.min(0.85, concealmentFactor));
    }

    public double getConcealmentFactor() {
        return concealmentFactor;
    }

    @Override
    public void update(SpatialGrid<Entity> grid, double deltaSeconds) throws EntityOutOfBoundsException {
        wrapped.update(grid, deltaSeconds);
    }

    @Override
    public void render(Graphics2D g) {
        wrapped.render(g);

        if (wrapped.isAlive()) {
            int r = (int) wrapped.getRadius() + 3;
            int x = (int) (getPosition().getX() - r);
            int y = (int) (getPosition().getY() - r);

            Stroke prevStroke = g.getStroke();
            g.setColor(CAMO_TINT);
            g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3, 3}, 0));
            g.drawOval(x, y, r * 2, r * 2);
            g.setStroke(prevStroke);
        }
    }
}
