package com.ecosystem.model.entities;

import com.ecosystem.core.Vector2D;
import com.ecosystem.events.EcosystemEvent;
import com.ecosystem.events.EcosystemEventBus;
import com.ecosystem.events.EcosystemEventType;
import com.ecosystem.exceptions.EntityOutOfBoundsException;
import com.ecosystem.exceptions.InvalidGenomeException;
import com.ecosystem.grid.SpatialGrid;
import com.ecosystem.model.Entity;
import com.ecosystem.model.genetics.Genome;
import com.ecosystem.model.strategy.AmbushStrategy;

import java.awt.Color;
import java.util.Optional;

/**
 * Tertiary apex predator commanding the top of the trophic pyramid.
 * Interacts via State and Strategy patterns.
 */
public class ApexPredator extends Animal {
    private static final Color APEX_COLOR = new Color(140, 20, 180);

    public ApexPredator(Vector2D position, double initialEnergy, Genome genome, int generation) {
        super(
                position,
                Math.max(9.0, genome.getSize() * 1.6),
                APEX_COLOR,
                initialEnergy,
                220.0,
                110.0, // Max age
                140.0, // Reproduction threshold
                genome,
                generation);
        setMovementStrategy(new AmbushStrategy());
    }

    public static ApexPredator createDefault(Vector2D position) {
        try {
            Genome defaultGenome = new Genome(75.0, 160.0, 9.0, 1.4, 0.04);
            return new ApexPredator(position, 130.0, defaultGenome, 1);
        } catch (InvalidGenomeException e) {
            throw new RuntimeException("Default apex predator genome initialization failed", e);
        }
    }

    @Override
    public Optional<? extends Entity> findThreat(SpatialGrid<Entity> grid) {
        // Apex predators have no natural predators in this ecosystem
        return Optional.empty();
    }

    @Override
    public Optional<? extends Entity> findFood(SpatialGrid<Entity> grid) {
        double vision = getGenome().getVisionRadius();
        // Prioritize carnivores for higher caloric density, then herbivores
        Optional<Carnivore> carnivore = grid.findNearest(getPosition(), vision, Carnivore.class, Entity::isAlive);
        if (carnivore.isPresent())
            return carnivore;
        return grid.findNearest(getPosition(), vision, Herbivore.class, Entity::isAlive);
    }

    @Override
    public Optional<? extends Animal> findMate(SpatialGrid<Entity> grid) {
        double vision = getGenome().getVisionRadius();
        return grid.findNearest(getPosition(), vision, ApexPredator.class,
                mate -> mate != this && mate.isAlive() && mate.canReproduce());
    }

    @Override
    public void consumeTarget(Entity food, SpatialGrid<Entity> grid) {
        if (food instanceof Animal prey && prey.isAlive()) {
            addEnergy(prey.getEnergy() * 0.85 + 40.0);
            prey.markDead();
            try {
                grid.addEntity(new Carcass(prey.getPosition(), 25.0));
            } catch (EntityOutOfBoundsException ignored) {
            }

            EcosystemEventBus.getInstance().publish(EcosystemEvent.of(
                    EcosystemEventType.PREDATION_OCCURRED,
                    "Apex Predator struck down " + prey.getClass().getSimpleName(),
                    getPosition(),
                    0,
                    this));
        }
    }

    @Override
    public Animal breed(Animal mate) {
        if (!(mate instanceof ApexPredator apexMate))
            return null;
        try {
            Genome childGenome = getGenome().crossover(apexMate.getGenome()).mutate();
            double offsetAngle = RNG.nextDouble() * 2 * Math.PI;
            Vector2D babyPos = getPosition().add(new Vector2D(Math.cos(offsetAngle) * 12, Math.sin(offsetAngle) * 12));
            int childGen = Math.max(getGeneration(), apexMate.getGeneration()) + 1;
            ApexPredator baby = new ApexPredator(babyPos, 80.0, childGenome, childGen);
            EcosystemEventBus.getInstance().publish(EcosystemEvent.of(
                    EcosystemEventType.ORGANISM_BORN,
                    "Dominant Apex Predator born (Gen " + childGen + ")",
                    babyPos,
                    0,
                    baby));
            return baby;
        } catch (InvalidGenomeException e) {
            return null;
        }
    }

    @Override
    public ApexPredator reproduce(SpatialGrid<Entity> grid) {
        return (ApexPredator) breed(this);
    }

    @Override
    public String getSpeciesName() {
        return "Apex Predator";
    }

    @Override
    protected void onDeath() {
        // Handled in engine
    }
}
