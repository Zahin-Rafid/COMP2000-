package com.ecosystem.model.decorator;

import com.ecosystem.exceptions.EntityOutOfBoundsException;
import com.ecosystem.grid.SpatialGrid;
import com.ecosystem.model.Entity;
import com.ecosystem.model.Organism;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Concrete decorator applying environmental camouflage to an organism.
 * Blends visual profile into the terrain, rendering a translucent camouflage ring.
 */
public class CamouflageBuff extends OrganismDecorator {
    private static final Color CAMO_TINT = new Color(34, 139, 34, 110);
    private final double concealmentFactor;

    public CamouflageBuff(Organism wrapped) {
        this(wrapped, 0.5); // 50% concealment
    }

    public CamouflageBuff(Organism wrapped, double concealmentFactor) {
        super(wrapped);
        this.concealmentFactor = concealmentFactor;
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
            int r = (int) wrapped.getRadius() + 2;
            int x = (int) (getPosition().getX() - r);
            int y = (int) (getPosition().getY() - r);

            // Shimmering camouflage boundary
            g.setColor(CAMO_TINT);
            g.drawOval(x, y, r * 2, r * 2);
        }
    }
}

