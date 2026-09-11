package com.ecosystem.model.entities;

import com.ecosystem.core.Vector2D;
import com.ecosystem.exceptions.EntityOutOfBoundsException;
import com.ecosystem.exceptions.InvalidGenomeException;
import com.ecosystem.exceptions.ResourceDepletedException;
import com.ecosystem.grid.Cell;
import com.ecosystem.grid.SpatialGrid;
import com.ecosystem.model.Entity;
import com.ecosystem.model.Organism;
import com.ecosystem.model.genetics.Genome;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Optional;
import java.util.Random;

/**
 * Detritivore/decomposer organism that consumes carcasses and fertilizes the soil.
 */
public class Decomposer extends Organism {
    private static final Random RNG = new Random();
    private static final Color DECOMPOSER_COLOR = new Color(200, 160, 40);

    public Decomposer(Vector2D position, double initialEnergy, Genome genome, int generation) {
        super(
                position,
                Math.max(3.0, genome.getSize()),
                DECOMPOSER_COLOR,
                initialEnergy,
                80.0,
                100.0,
                50.0,
                genome,
                generation
        );
    }

    public static Decomposer createDefault(Vector2D position) {
        try {
            Genome defaultGenome = new Genome(0.0, 40.0, 3.5, 0.6, 0.05);
            return new Decomposer(position, 30.0, defaultGenome, 1);
        } catch (InvalidGenomeException e) {
            throw new RuntimeException("Default decomposer genome initialization failed", e);
        }
    }

    @Override
    public void update(SpatialGrid<Entity> grid, double deltaSeconds) throws EntityOutOfBoundsException {
        if (!isAlive()) return;

        burnBaseMetabolism(deltaSeconds * 0.15);

        // Decompose nearby carcasses
        double vision = getGenome().getVisionRadius();
        Optional<Carcass> carcass = grid.findNearest(getPosition(), vision, Carcass.class, Entity::isAlive);

        if (carcass.isPresent()) {
            Carcass food = carcass.get();
            try {
                double harvested = food.harvestBiomass(15.0 * deltaSeconds);
                addEnergy(harvested);

                // Deposit nutrients back into the soil
                Cell cell = grid.getCellAtWorldCoords(getPosition());
                if (cell != null) {
                    cell.getSoilNutrients().replenish(harvested * 0.7);
                }
            } catch (ResourceDepletedException e) {
                // Carcass consumed
            }
        }

        if (canReproduce()) {
            Decomposer child = reproduce(grid);
            if (child != null) {
                grid.addEntity(child);
                consumeEnergy(getReproductionThreshold() * 0.5);
            }
        }
    }

    @Override
    public Decomposer reproduce(SpatialGrid<Entity> grid) {
        try {
            double angle = RNG.nextDouble() * 2 * Math.PI;
            double dist = 15.0 + RNG.nextDouble() * 20.0;
            Vector2D babyPos = getPosition().add(new Vector2D(Math.cos(angle) * dist, Math.sin(angle) * dist));
            if (!grid.isWithinBounds(babyPos)) {
                return null;
            }
            Genome childGenome = getGenome().mutate();
            return new Decomposer(babyPos, 20.0, childGenome, getGeneration() + 1);
        } catch (InvalidGenomeException e) {
            return null;
        }
    }

    @Override
    public void render(Graphics2D g) {
        if (!isAlive()) return;
        int r = (int) getRadius();
        int x = (int) (getPosition().getX() - r);
        int y = (int) (getPosition().getY() - r);

        g.setColor(getColor());
        // Draw mushroom/spore-like shape
        g.fillArc(x, y - 2, r * 2, r * 2, 0, 180);
        g.fillRect(x + r / 2, y + r / 2, r, r);
    }
}
