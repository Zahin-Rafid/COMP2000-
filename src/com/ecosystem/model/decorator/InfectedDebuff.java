package com.ecosystem.model.decorator;

import com.ecosystem.events.EcosystemEvent;
import com.ecosystem.events.EcosystemEventBus;
import com.ecosystem.events.EcosystemEventType;
import com.ecosystem.exceptions.EntityOutOfBoundsException;
import com.ecosystem.grid.SpatialGrid;
import com.ecosystem.model.Entity;
import com.ecosystem.model.Organism;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.List;

/**
 * Concrete decorator applying a pathogenic infection debuff to an organism.
 * Drains metabolic energy at an accelerated rate and transmits pathogens
 * to adjacent organisms via spatial proximity.
 */
public class InfectedDebuff extends OrganismDecorator {
    private static final Color SICKLY_TINT = new Color(110, 240, 40, 210);
    private final double infectionDrainRate;
    private double pulsePhase = 0.0;

    public InfectedDebuff(Organism wrapped) {
        this(wrapped, 2.5);
    }

    public InfectedDebuff(Organism wrapped, double infectionDrainRate) {
        super(wrapped);
        this.infectionDrainRate = infectionDrainRate;
    }

    @Override
    public void update(SpatialGrid<Entity> grid, double deltaSeconds) throws EntityOutOfBoundsException {
        if (!wrapped.isAlive()) return;
        pulsePhase += deltaSeconds * 6.0;

        // Additional metabolic decay caused by infection
        wrapped.consumeEnergy(infectionDrainRate * deltaSeconds);

        // Contagion mechanism: attempt spreading pathogen to uninfected organisms within close contact
        if (wrapped.isAlive()) {
            double contagionRadius = wrapped.getRadius() * 2.5 + 6.0;
            List<Organism> nearby = grid.queryEntitiesNear(getPosition(), contagionRadius, Organism.class);
            for (Organism other : nearby) {
                if (other != this && other != wrapped && other.isAlive()
                        && !OrganismDecorator.hasDecorator(other, InfectedDebuff.class)) {
                    // Transmission chance scaled by time step
                    if (Math.random() < 0.25 * deltaSeconds) {
                        grid.replaceEntity(other, new InfectedDebuff(other, infectionDrainRate));
                        EcosystemEventBus.getInstance().publish(EcosystemEvent.of(
                                EcosystemEventType.TROPHIC_COLLAPSE_WARNING,
                                "Pathogen spread to " + other.getSpeciesName(),
                                other.getPosition(),
                                0,
                                other
                        ));
                        break; // Max one transmission per tick per infected host
                    }
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
            int pulse = (int) (Math.sin(pulsePhase) * 1.5);
            int r = (int) wrapped.getRadius() + 4 + pulse;
            int x = (int) (getPosition().getX() - r);
            int y = (int) (getPosition().getY() - r);

            Stroke prevStroke = g.getStroke();
            g.setColor(SICKLY_TINT);
            g.setStroke(new BasicStroke(2.0f));
            g.drawOval(x, y, r * 2, r * 2);
            g.setStroke(prevStroke);
        }
    }
}
