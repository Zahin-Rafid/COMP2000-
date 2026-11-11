package com.ecosystem.model.entities;

import com.ecosystem.core.Vector2D;
import com.ecosystem.events.EcosystemEvent;
import com.ecosystem.events.EcosystemEventBus;
import com.ecosystem.events.EcosystemEventType;
import com.ecosystem.exceptions.EntityOutOfBoundsException;
import com.ecosystem.exceptions.InvalidGenomeException;
import com.ecosystem.exceptions.ResourceDepletedException;
import com.ecosystem.grid.SpatialGrid;
import com.ecosystem.model.Entity;
import com.ecosystem.model.genetics.Genome;
import com.ecosystem.model.strategy.AmbushStrategy;

import java.awt.Color;
import java.util.Optional;

/**
 * Secondary consumer (carnivore/predator) that hunts herbivores and scavenges carcasses.
 * Driven by the State and Strategy design patterns.
 */
public class Carnivore extends Animal {
    private static final Color CARNIVORE_COLOR = new Color(220, 50, 50);

    public Carnivore(Vector2D position, double initialEnergy, Genome genome, int generation) {
        super(
                position,
                Math.max(6.0, genome.getSize() * 1.2),
                CARNIVORE_COLOR,
                initialEnergy,
                150.0,
                90.0, // Max age
                95.0, // Reproduction threshold
                genome,
                generation,
                new AmbushStrategy()
        );
    }

    public static Carnivore createDefault(Vector2D position) {
        try {
            Genome defaultGenome = new Genome(85.0, 130.0, 6.5, 1.2, 0.06);
            return new Carnivore(position, 85.0, defaultGenome, 1);
        } catch (InvalidGenomeException e) {
            throw new RuntimeException("Default carnivore genome initialization failed", e);
        }
    }

    @Override
    public Optional<? extends Entity> findThreat(SpatialGrid<Entity> grid) {
        double vision = getGenome().getVisionRadius();
        return grid.findNearest(getPosition(), vision * 0.85, ApexPredator.class, Entity::isAlive);
    }

    @Override
    public Optional<? extends Entity> findFood(SpatialGrid<Entity> grid) {
        double vision = getGenome().getVisionRadius();
        // Prioritize live herbivores
        Optional<Herbivore> prey = grid.findNearest(getPosition(), vision, Herbivore.class, Entity::isAlive);
        if (prey.isPresent()) return prey;

        // Fallback to scavenging carcasses if energy < 65%
        if (getEnergy() < getMaxEnergy() * 0.65) {
            return grid.findNearest(getPosition(), vision * 0.8, Carcass.class, Entity::isAlive);
        }
        return Optional.empty();
    }

    @Override
    public Optional<? extends Animal> findMate(SpatialGrid<Entity> grid) {
        double vision = getGenome().getVisionRadius();
        return grid.findNearest(getPosition(), vision, Carnivore.class,
                mate -> mate != this && mate.isAlive() && mate.canReproduce());
    }

    @Override
    public void consumeTarget(Entity food, SpatialGrid<Entity> grid) {
        if (food instanceof Herbivore herbivore && herbivore.isAlive()) {
            addEnergy(herbivore.getEnergy() * 0.9 + 25.0);
            herbivore.markDead();
            // Drop carcass remains
            try {
                grid.addEntity(new Carcass(herbivore.getPosition(), 15.0));
            } catch (EntityOutOfBoundsException e) {
                System.err.println("Skipped out-of-bounds carcass drop: " + e.getMessage());
            }

            EcosystemEventBus.getInstance().publish(EcosystemEvent.of(
                    EcosystemEventType.PREDATION_OCCURRED,
                    "Carnivore successfully hunted Herbivore",
                    getPosition(),
                    0,
                    this
            ));
        } else if (food instanceof Carcass carcass && carcass.isAlive()) {
            try {
                double harvested = carcass.harvestBiomass(20.0);
                addEnergy(harvested * 1.8);
            } catch (ResourceDepletedException e) {
                carcass.markDead();
            }
        }
    }

    @Override
    public Animal breed(Animal mate) {
        if (!(mate instanceof Carnivore carnMate)) return null;
        try {
            Genome childGenome = getGenome().crossover(carnMate.getGenome()).mutate();
            double offsetAngle = RNG.nextDouble() * 2 * Math.PI;
            Vector2D babyPos = getPosition().add(new Vector2D(Math.cos(offsetAngle) * 10, Math.sin(offsetAngle) * 10));
            int childGen = Math.max(getGeneration(), carnMate.getGeneration()) + 1;
            Carnivore baby = new Carnivore(babyPos, 50.0, childGenome, childGen);
            EcosystemEventBus.getInstance().publish(EcosystemEvent.of(
                    EcosystemEventType.ORGANISM_BORN,
                    "New Carnivore born (Gen " + childGen + ")",
                    babyPos,
                    0,
                    baby
            ));
            return baby;
        } catch (InvalidGenomeException e) {
            System.err.println("Skipped invalid Carnivore genome during breeding: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Carnivore reproduce(SpatialGrid<Entity> grid) {
        return (Carnivore) breed(this);
    }

    @Override
    public String getSpeciesName() {
        return "Carnivore";
    }

    @Override
    protected void onDeath() {
        // Handled in engine
    }
}
