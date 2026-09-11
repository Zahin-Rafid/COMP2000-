package com.ecosystem.grid;

import com.ecosystem.core.Vector2D;
import com.ecosystem.exceptions.EntityOutOfBoundsException;
import com.ecosystem.model.Entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Generic spatial data structure partitioning entities into spatial buckets for high-performance
 * collision and sensory queries. Demonstrates generic type bounds <T extends Entity>.
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
    private final List<T> pendingAdditions;
    private final List<T> pendingRemovals;

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
        this.pendingAdditions = new ArrayList<>();
        this.pendingRemovals = new ArrayList<>();

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
        // Procedural biome layout: central fertile plains, corner water bodies, north forest, south desert
        double nx = (double) x / cols;
        double ny = (double) y / rows;

        // Lake in top-right
        if (nx > 0.75 && ny < 0.3) {
            return BiomeType.WATER;
        }
        // Lake in bottom-left
        if (nx < 0.25 && ny > 0.75) {
            return BiomeType.WATER;
        }
        // Forest along the top
        if (ny < 0.35) {
            return BiomeType.FOREST;
        }
        // Arid desert along the bottom
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

    public synchronized void addEntity(T entity) throws EntityOutOfBoundsException {
        if (!isWithinBounds(entity.getPosition())) {
            throw new EntityOutOfBoundsException(entity.getPosition(), worldWidth, worldHeight);
        }
        pendingAdditions.add(entity);
    }

    public synchronized void removeEntity(T entity) {
        pendingRemovals.add(entity);
    }

    public synchronized void synchronizeEntities() {
        if (!pendingAdditions.isEmpty()) {
            allEntities.addAll(pendingAdditions);
            pendingAdditions.clear();
        }
        if (!pendingRemovals.isEmpty()) {
            allEntities.removeAll(pendingRemovals);
            pendingRemovals.clear();
        }
        // Purge dead entities
        allEntities.removeIf(e -> !e.isAlive());
    }

    public List<T> getAllEntities() {
        return Collections.unmodifiableList(allEntities);
    }

    /**
     * Generic query method returning all entities matching a specific class or subclass.
     *
     * @param typeClass The target class to filter by.
     * @param <E> The specific subtype of T requested.
     * @return Filtered list of entities of type E.
     */
    @SuppressWarnings("unchecked")
    public <E extends T> List<E> queryEntities(Class<E> typeClass) {
        List<E> result = new ArrayList<>();
        for (T entity : allEntities) {
            if (entity.isAlive() && typeClass.isInstance(entity)) {
                result.add((E) entity);
            }
        }
        return result;
    }

    /**
     * Generic spatial query method returning all entities within a circular radius matching a type.
     *
     * @param center Query origin.
     * @param radius Maximum search distance.
     * @param typeClass Target entity class.
     * @param <E> Subtype of T.
     * @return Entities within distance matching type E.
     */
    @SuppressWarnings("unchecked")
    public <E extends T> List<E> queryEntitiesNear(Vector2D center, double radius, Class<E> typeClass) {
        List<E> result = new ArrayList<>();
        double radiusSq = radius * radius;

        for (T entity : allEntities) {
            if (entity.isAlive() && typeClass.isInstance(entity)) {
                double distSq = Math.pow(entity.getPosition().getX() - center.getX(), 2) +
                                Math.pow(entity.getPosition().getY() - center.getY(), 2);
                if (distSq <= radiusSq) {
                    result.add((E) entity);
                }
            }
        }
        return result;
    }

    /**
     * Finds the closest entity matching the specified type and predicate condition.
     */
    public <E extends T> Optional<E> findNearest(Vector2D center, double maxRadius, Class<E> typeClass, Predicate<E> filter) {
        List<E> candidates = queryEntitiesNear(center, maxRadius, typeClass);
        E bestMatch = null;
        double minDistance = Double.MAX_VALUE;

        for (E candidate : candidates) {
            if (filter == null || filter.test(candidate)) {
                double dist = center.distanceTo(candidate.getPosition());
                if (dist < minDistance) {
                    minDistance = dist;
                    bestMatch = candidate;
                }
            }
        }
        return Optional.ofNullable(bestMatch);
    }

    public void updateEnvironment(double regenRate) {
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                cells[x][y].update(regenRate);
            }
        }
    }
}
