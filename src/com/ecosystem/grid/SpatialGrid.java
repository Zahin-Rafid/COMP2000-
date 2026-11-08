package com.ecosystem.grid;

import com.ecosystem.core.Vector2D;
import com.ecosystem.exceptions.EntityOutOfBoundsException;
import com.ecosystem.model.Entity;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;

/**
 * Generic spatial data structure partitioning entities into spatial buckets for high-performance
 * collision and sensory queries. Demonstrates generic type bounds {@code <T extends Entity>}
 * and thread-safe concurrent collection buffering for parallel stream execution.
 *
 * @param <T> The entity type contained within the grid.
 */
public class SpatialGrid<T extends Entity> {
    private final double worldWidth;
    private final double worldHeight;
    private final double cellSize;
    private final int cols;
    private final int rows;

    private final Cell[][] cells;
    private final List<T> allEntities;
    private final Queue<T> pendingAdditions;
    private final Queue<T> pendingRemovals;

    public SpatialGrid(double worldWidth, double worldHeight, double cellSize) {
        if (worldWidth <= 0 || worldHeight <= 0 || cellSize <= 0) {
            throw new IllegalArgumentException("Grid dimensions and cell size must be positive.");
        }
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.cellSize = cellSize;
        this.cols = (int) Math.ceil(worldWidth / cellSize);
        this.rows = (int) Math.ceil(worldHeight / cellSize);

        this.cells = new Cell[cols][rows];
        this.allEntities = new ArrayList<>();
        // Concurrent queues prevent race conditions when multiple threads queue entities
        this.pendingAdditions = new ConcurrentLinkedQueue<>();
        this.pendingRemovals = new ConcurrentLinkedQueue<>();

        initializeCells();
    }

    private void initializeCells() {
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                BiomeType biome = determineBiome(x, y);
                cells[x][y] = new Cell(x, y, cellSize, biome);
            }
        }
    }

    private BiomeType determineBiome(int x, int y) {
        double nx = (double) x / cols;
        double ny = (double) y / rows;

        if (nx > 0.75 && ny < 0.3) {
            return BiomeType.WATER;
        }
        if (nx < 0.25 && ny > 0.75) {
            return BiomeType.WATER;
        }
        if (ny < 0.35) {
            return BiomeType.FOREST;
        }
        if (ny > 0.75) {
            return BiomeType.DESERT;
        }
        return BiomeType.FERTILE_SOIL;
    }

    public double getWorldWidth() {
        return worldWidth;
    }

    public double getWorldHeight() {
        return worldHeight;
    }

    public double getCellSize() {
        return cellSize;
    }

    public int getCols() {
        return cols;
    }

    public int getRows() {
        return rows;
    }

    public Cell getCellAtGridCoords(int col, int row) {
        if (col >= 0 && col < cols && row >= 0 && row < rows) {
            return cells[col][row];
        }
        return null;
    }

    public Cell getCellAtWorldCoords(Vector2D pos) {
        int col = (int) (pos.getX() / cellSize);
        int row = (int) (pos.getY() / cellSize);
        return getCellAtGridCoords(col, row);
    }

    public boolean isWithinBounds(Vector2D position) {
        return position.getX() >= 0 && position.getX() <= worldWidth &&
               position.getY() >= 0 && position.getY() <= worldHeight;
    }

    public void addEntity(T entity) throws EntityOutOfBoundsException {
        if (!isWithinBounds(entity.getPosition())) {
            throw new EntityOutOfBoundsException(entity.getPosition(), worldWidth, worldHeight);
        }
        pendingAdditions.add(entity);
    }

    public void removeEntity(T entity) {
        pendingRemovals.add(entity);
    }

    public synchronized void synchronizeEntities() {
        T addition;
        while ((addition = pendingAdditions.poll()) != null) {
            allEntities.add(addition);
        }
        T removal;
        while ((removal = pendingRemovals.poll()) != null) {
            allEntities.remove(removal);
        }
        // Purge dead entities via functional predicate
        allEntities.removeIf(e -> !e.isAlive());
    }

    public synchronized List<T> getAllEntities() {
        return Collections.unmodifiableList(new ArrayList<>(allEntities));
    }

    public synchronized List<T> getAllEntitiesSnapshot() {
        return new ArrayList<>(allEntities);
    }

    /**
     * Generic query method returning all entities matching a specific class or subclass.
     * Expressed via declarative Streams API.
     */
    @SuppressWarnings("unchecked")
    public <E extends T> List<E> queryEntities(Class<E> typeClass) {
        synchronized (this) {
            return allEntities.stream()
                    .filter(Entity::isAlive)
                    .filter(typeClass::isInstance)
                    .map(e -> (E) e)
                    .toList();
        }
    }

    /**
     * Generic spatial query method returning all entities within a circular radius matching a type.
     * Uses Streams API with distance squared filtering to avoid expensive square-root calls.
     */
    @SuppressWarnings("unchecked")
    public <E extends T> List<E> queryEntitiesNear(Vector2D center, double radius, Class<E> typeClass) {
        double radiusSq = radius * radius;
        synchronized (this) {
            return allEntities.stream()
                    .filter(Entity::isAlive)
                    .filter(typeClass::isInstance)
                    .filter(e -> {
                        double dx = e.getPosition().getX() - center.getX();
                        double dy = e.getPosition().getY() - center.getY();
                        return (dx * dx + dy * dy) <= radiusSq;
                    })
                    .map(e -> (E) e)
                    .toList();
        }
    }

    /**
     * Finds the closest entity matching the specified type and predicate condition.
     * Implemented using a concise, declarative stream pipeline with Comparator.comparingDouble.
     */
    public <E extends T> Optional<E> findNearest(Vector2D center, double maxRadius, Class<E> typeClass, Predicate<E> filter) {
        return queryEntitiesNear(center, maxRadius, typeClass).stream()
                .filter(candidate -> filter == null || filter.test(candidate))
                .min(Comparator.comparingDouble(candidate -> center.distanceTo(candidate.getPosition())));
    }

    public void updateEnvironment(double regenRate) {
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                cells[x][y].update(regenRate);
            }
        }
    }
}
