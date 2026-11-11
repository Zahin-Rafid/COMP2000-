package com.ecosystem.model.entities;

import com.ecosystem.core.Vector2D;
import com.ecosystem.events.EcosystemEvent;
import com.ecosystem.events.EcosystemEventBus;
import com.ecosystem.events.EcosystemEventType;
import com.ecosystem.exceptions.InvalidGenomeException;
import com.ecosystem.grid.SpatialGrid;
import com.ecosystem.model.Entity;
import com.ecosystem.model.genetics.Genome;
import com.ecosystem.model.strategy.FlockingStrategy;

import java.awt.Color;
import java.util.Optional;

/**
 * Primary consumer (herbivore) that grazes on autotrophic plants, herds with conspecifics,
 * and flees from secondary/tertiary predators using the State and Strategy patterns.
 */
public class Herbivore extends Animal {
    private static final Color HERBIVORE_COLOR = new Color(50, 150, 250);

    public Herbivore(Vector2D position, double initialEnergy, Genome genome, int generation) {
        super(
                position,
                Math.max(4.0, genome.getSize()),
                HERBIVORE_COLOR,
                initialEnergy,
                120.0,
                70.0, // Max age
                75.0, // Reproduction threshold
                genome,
                generation,
                new FlockingStrategy(genome.getVisionRadius() * 0.6)
        );
    }

    public static Herbivore createDefault(Vector2D position) {
        try {
            Genome defaultGenome = new Genome(65.0, 95.0, 5.0, 1.0, 0.08);
            return new Herbivore(position, 65.0, defaultGenome, 1);
        } catch (InvalidGenomeException e) {
            throw new RuntimeException("Default herbivore genome initialization failed", e);
        }
    }

    @Override
    public Optional<? extends Entity> findThreat(SpatialGrid<Entity> grid) {
        double vision = getGenome().getVisionRadius();
        Optional<ApexPredator> apex = grid.findNearest(getPosition(), vision * 1.1, ApexPredator.class, Entity::isAlive);
        if (apex.isPresent()) return apex;
        return grid.findNearest(getPosition(), vision * 0.9, Carnivore.class, Entity::isAlive);
    }

    @Override
    public Optional<? extends Entity> findFood(SpatialGrid<Entity> grid) {
        double vision = getGenome().getVisionRadius();
        return grid.findNearest(getPosition(), vision, Plant.class, Entity::isAlive);
    }

    @Override
    public Optional<? extends Animal> findMate(SpatialGrid<Entity> grid) {
        double vision = getGenome().getVisionRadius();
        return grid.findNearest(getPosition(), vision, Herbivore.class,
                mate -> mate != this && mate.isAlive() && mate.canReproduce());
    }

    @Override
    public void consumeTarget(Entity food, SpatialGrid<Entity> grid) {
        if (food instanceof Plant plant && plant.isAlive()) {
            addEnergy(Math.max(25.0, plant.getEnergy() * 1.1));
            plant.markDead();
            EcosystemEventBus.getInstance().publish(EcosystemEvent.of(
                    EcosystemEventType.PREDATION_OCCURRED,
                    "Herbivore grazed on Plant",
                    getPosition(),
                    0,
                    this
            ));
        }
    }

    @Override
    public Animal breed(Animal mate) {
        if (!(mate instanceof Herbivore herbMate)) return null;
        try {
            Genome childGenome = getGenome().crossover(herbMate.getGenome()).mutate();
            double offsetAngle = RNG.nextDouble() * 2 * Math.PI;
            Vector2D babyPos = getPosition().add(new Vector2D(Math.cos(offsetAngle) * 8, Math.sin(offsetAngle) * 8));
            int childGen = Math.max(getGeneration(), herbMate.getGeneration()) + 1;
            Herbivore baby = new Herbivore(babyPos, 40.0, childGenome, childGen);
            EcosystemEventBus.getInstance().publish(EcosystemEvent.of(
                    EcosystemEventType.ORGANISM_BORN,
                    "New Herbivore born (Gen " + childGen + ")",
                    babyPos,
                    0,
                    baby
            ));
            return baby;
        } catch (InvalidGenomeException e) {
            System.err.println("Skipped invalid Herbivore genome during breeding: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Herbivore reproduce(SpatialGrid<Entity> grid) {
        return (Herbivore) breed(this);
    }

    @Override
    public String getSpeciesName() {
        return "Herbivore";
    }

    @Override
    protected void onDeath() {
        // Carcass drop managed upon predation or starvation in engine
    }
}
