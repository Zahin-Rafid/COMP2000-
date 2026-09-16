package com.ecosystem.model.entities;

import com.ecosystem.core.Vector2D;
import com.ecosystem.exceptions.EntityOutOfBoundsException;
import com.ecosystem.grid.Cell;
import com.ecosystem.grid.SpatialGrid;
import com.ecosystem.model.Entity;
import com.ecosystem.model.Organism;
import com.ecosystem.model.genetics.Genome;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

/**
 * Abstract heterotrophic organism capable of autonomous spatial navigation,
 * sensing, and hunting/foraging.
 */
public abstract class Animal extends Organism {
    protected static final Random RNG = new Random();

    private Vector2D velocity;
    private double headingAngle;
    private double wanderAngle;
    private boolean isSprinting;

    public Animal(Vector2D position, double radius, Color color, double initialEnergy,
            double maxEnergy, double maxAge, double reproductionThreshold,
            Genome genome, int generation) {
        super(position, radius, color, initialEnergy, maxEnergy, maxAge, reproductionThreshold, genome, generation);
        this.velocity = new Vector2D(0, 0);
        this.headingAngle = RNG.nextDouble() * 2 * Math.PI;
        this.wanderAngle = this.headingAngle;
        this.isSprinting = false;
    }

    public Vector2D getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector2D velocity) {
        this.velocity = velocity;
    }

    public double getHeadingAngle() {
        return headingAngle;
    }

    public boolean isSprinting() {
        return isSprinting;
    }

    public void setSprinting(boolean sprinting) {
        isSprinting = sprinting;
    }

    /**
     * Steers the animal towards a target position with speed determined by its
     * genome.
     */
    protected Vector2D calculateSteeringForce(Vector2D targetPos, double speedMultiplier) {
        Vector2D desired = targetPos.subtract(getPosition());
        double dist = desired.magnitude();
        if (dist < 0.001) {
            return new Vector2D(0, 0);
        }
        double targetSpeed = getGenome().getSpeed() * speedMultiplier;
        Vector2D desiredVel = desired.normalize().multiply(targetSpeed);
        return desiredVel.subtract(this.velocity);
    }

    /**
     * Steers the animal directly away from a threat coordinate.
     */
    protected Vector2D calculateFleeForce(Vector2D threatPos, double speedMultiplier) {
        Vector2D desired = getPosition().subtract(threatPos);
        double dist = desired.magnitude();
        if (dist < 0.001) {
            return new Vector2D(1, 0);
        }
        double targetSpeed = getGenome().getSpeed() * speedMultiplier;
        Vector2D desiredVel = desired.normalize().multiply(targetSpeed);
        return desiredVel.subtract(this.velocity);
    }

    /**
     * Generates a smooth organic wandering force when no immediate targets or
     * threats exist.
     */
    protected Vector2D calculateWanderForce() {
        wanderAngle += (RNG.nextDouble() - 0.5) * 0.8;
        double wanderRadius = 15.0;
        double circleDist = 25.0;

        Vector2D circleCenter = new Vector2D(Math.cos(headingAngle), Math.sin(headingAngle)).multiply(circleDist);
        Vector2D displacement = new Vector2D(Math.cos(wanderAngle), Math.sin(wanderAngle)).multiply(wanderRadius);
        return circleCenter.add(displacement);
    }

    /**
     * Advances the animal's position, checks boundaries, and drains kinetic energy.
     */
    protected void applyKineticMovement(SpatialGrid<Entity> grid, Vector2D force, double deltaSeconds)
            throws EntityOutOfBoundsException {
        // Accelerate
        this.velocity = this.velocity.add(force.multiply(deltaSeconds * 4.0));

        // Limit velocity to max speed
        double maxSpeed = getGenome().getSpeed() * (isSprinting ? 1.4 : 0.8);
        if (this.velocity.magnitude() > maxSpeed) {
            this.velocity = this.velocity.normalize().multiply(maxSpeed);
        }

        // Update heading if moving
        if (this.velocity.magnitude() > 0.5) {
            this.headingAngle = Math.atan2(this.velocity.getY(), this.velocity.getX());
        }

        // Check biome resistance
        Cell currentCell = grid.getCellAtWorldCoords(getPosition());
        double speedPenalty = 1.0;
        if (currentCell != null) {
            if (currentCell.getBiome().getColor().equals(Color.BLUE) || !currentCell.getBiome().isTraversableByLand()) {
                speedPenalty = 0.35; // Water slows heavily
            } else if (currentCell.getBiome() == com.ecosystem.grid.BiomeType.DESERT) {
                speedPenalty = 0.75;
            }
        }

        Vector2D displacement = this.velocity.multiply(deltaSeconds * speedPenalty);
        Vector2D newPos = getPosition().add(displacement);

        // Clamping to world bounds
        double pad = getRadius() + 2.0;
        double clampedX = Math.max(pad, Math.min(grid.getWorldWidth() - pad, newPos.getX()));
        double clampedY = Math.max(pad, Math.min(grid.getWorldHeight() - pad, newPos.getY()));

        // Bounce off walls
        if (clampedX != newPos.getX()) {
            this.velocity = new Vector2D(-this.velocity.getX() * 0.8, this.velocity.getY());
        }
        if (clampedY != newPos.getY()) {
            this.velocity = new Vector2D(this.velocity.getX(), -this.velocity.getY() * 0.8);
        }

        setPosition(new Vector2D(clampedX, clampedY));

        // Energy consumption includes base metabolism + kinetic movement cost
        double moveEnergyCost = (this.velocity.magnitude() / 50.0) * (isSprinting ? 2.5 : 1.0) * deltaSeconds;
        burnBaseMetabolism(deltaSeconds);
        consumeEnergy(moveEnergyCost);
    }

    @Override
    public void render(Graphics2D g) {
        if (!isAlive())
            return;
        int r = (int) getRadius();
        int x = (int) (getPosition().getX() - r);
        int y = (int) (getPosition().getY() - r);

        // Body circle
        g.setColor(getColor());
        g.fillOval(x, y, r * 2, r * 2);
        g.setColor(Color.BLACK);
        g.drawOval(x, y, r * 2, r * 2);

        // Directional nose / pointer
        int noseX = (int) (getPosition().getX() + Math.cos(headingAngle) * (r + 3));
        int noseY = (int) (getPosition().getY() + Math.sin(headingAngle) * (r + 3));
        g.setColor(Color.WHITE);
        g.drawLine((int) getPosition().getX(), (int) getPosition().getY(), noseX, noseY);

        // Energy bar above entity
        int barW = Math.max(12, r * 2);
        int barH = 3;
        int barX = (int) (getPosition().getX() - barW / 2.0);
        int barY = y - 5;

        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(barX, barY, barW, barH);
        float energyRatio = (float) Math.max(0.0, Math.min(1.0, getEnergy() / getMaxEnergy()));
        g.setColor(energyRatio > 0.5 ? Color.GREEN : (energyRatio > 0.25 ? Color.YELLOW : Color.RED));
        g.fillRect(barX, barY, (int) (barW * energyRatio), barH);
    }
}
