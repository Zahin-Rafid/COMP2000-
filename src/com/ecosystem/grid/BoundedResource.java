package com.ecosystem.grid;

import com.ecosystem.exceptions.ResourceDepletedException;

/**
 * Generic wrapper for a quantifiable resource with strict upper and lower boundaries.
 * Demonstrates generics with bounded type parameter <T extends Number>.
 *
 * @param <T> The numeric type representing resource quantity (e.g. Double, Integer)
 */
public class BoundedResource<T extends Number> {
    private final String name;
    private final double maxCapacity;
    private double currentAmount;

    public BoundedResource(String name, double maxCapacity, double initialAmount) {
        if (maxCapacity <= 0) {
            throw new IllegalArgumentException("Maximum capacity must be positive: " + maxCapacity);
        }
        this.name = name;
        this.maxCapacity = maxCapacity;
        this.currentAmount = Math.min(Math.max(0, initialAmount), maxCapacity);
    }

    public String getName() {
        return name;
    }

    public double getMaxCapacity() {
        return maxCapacity;
    }

    public double getCurrentAmount() {
        return currentAmount;
    }

    public double getFillPercentage() {
        return (currentAmount / maxCapacity) * 100.0;
    }

    public synchronized void replenish(double amount) {
        if (amount > 0) {
            this.currentAmount = Math.min(this.maxCapacity, this.currentAmount + amount);
        }
    }

    public synchronized double consume(double requestedAmount) throws ResourceDepletedException {
        if (currentAmount <= 0) {
            throw new ResourceDepletedException(name, requestedAmount);
        }
        double consumed = Math.min(requestedAmount, currentAmount);
        currentAmount -= consumed;
        return consumed;
    }

    public synchronized boolean hasAvailable() {
        return currentAmount > 0.001;
    }

    @Override
    public String toString() {
        return String.format("%s: %.1f / %.1f (%.1f%%)", name, currentAmount, maxCapacity, getFillPercentage());
    }
}
