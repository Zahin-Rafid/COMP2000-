package com.ecosystem.ui;

import com.ecosystem.core.Engine;
import com.ecosystem.core.Vector2D;
import com.ecosystem.exceptions.EntityOutOfBoundsException;
import com.ecosystem.grid.Cell;
import com.ecosystem.grid.SpatialGrid;
import com.ecosystem.model.Entity;
import com.ecosystem.model.Organism;
import com.ecosystem.model.entities.*;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Custom JPanel drawing the 2D ecological terrain, entity layers, vision ranges, and interactive click spawning.
 */
public class SimulationPanel extends JPanel {
    private final Engine engine;
    private boolean showVisionHalos = false;
    private boolean showNutrientHeatmap = false;
    private String selectedSpawnTool = "Plant";

    public SimulationPanel(Engine engine) {
        this.engine = engine;
        setPreferredSize(new Dimension((int) engine.getConfig().worldWidth(), (int) engine.getConfig().worldHeight()));
        setBackground(new Color(25, 30, 35));

        setupMouseInteractions();
    }

    private void setupMouseInteractions() {
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                spawnEntityAt(new Vector2D(e.getX(), e.getY()));
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                spawnEntityAt(new Vector2D(e.getX(), e.getY()));
            }
        };
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    private void spawnEntityAt(Vector2D pos) {
        SpatialGrid<Entity> grid = engine.getGrid();
        if (!grid.isWithinBounds(pos)) return;

        try {
            switch (selectedSpawnTool) {
                case "Plant" -> grid.addEntity(Plant.createDefault(pos));
                case "Herbivore" -> grid.addEntity(Herbivore.createDefault(pos));
                case "Carnivore" -> grid.addEntity(Carnivore.createDefault(pos));
                case "Apex Predator" -> grid.addEntity(ApexPredator.createDefault(pos));
                case "Decomposer" -> grid.addEntity(Decomposer.createDefault(pos));
                case "Carcass" -> grid.addEntity(new Carcass(pos, 30.0));
            }
        } catch (EntityOutOfBoundsException ignored) {}
    }

    public void setShowVisionHalos(boolean show) {
        this.showVisionHalos = show;
    }

    public void setShowNutrientHeatmap(boolean show) {
        this.showNutrientHeatmap = show;
    }

    public void setSelectedSpawnTool(String tool) {
        this.selectedSpawnTool = tool;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        SpatialGrid<Entity> grid = engine.getGrid();
        int cols = grid.getCols();
        int rows = grid.getRows();
        double cellSize = grid.getCellSize();

        // 1. Draw Biome & Terrain Cells
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                Cell cell = grid.getCellAtGridCoords(x, y);
                if (cell != null) {
                    int px = (int) (x * cellSize);
                    int py = (int) (y * cellSize);
                    int sz = (int) Math.ceil(cellSize);

                    Color base = cell.getBiome().getColor();
                    if (showNutrientHeatmap && cell.getBiome().isTraversableByLand()) {
                        float pct = (float) (cell.getSoilNutrients().getCurrentAmount() / cell.getSoilNutrients().getMaxCapacity());
                        pct = Math.max(0.0f, Math.min(1.0f, pct));
                        g2.setColor(new Color((int) (base.getRed() * 0.4), (int) (50 + pct * 180), (int) (base.getBlue() * 0.4)));
                    } else {
                        g2.setColor(base);
                    }
                    g2.fillRect(px, py, sz, sz);
                }
            }
        }

        // 2. Draw Vision Halos if enabled
        if (showVisionHalos) {
            for (Entity entity : grid.getAllEntities()) {
                if (entity instanceof Organism org && entity.isAlive()) {
                    double vision = org.getGenome().getVisionRadius();
                    int vx = (int) (entity.getPosition().getX() - vision);
                    int vy = (int) (entity.getPosition().getY() - vision);
                    int vd = (int) (vision * 2);

                    Color haloColor = entity instanceof ApexPredator ? new Color(140, 20, 180, 20) :
                                     (entity instanceof Carnivore ? new Color(220, 50, 50, 20) :
                                     (entity instanceof Herbivore ? new Color(50, 150, 250, 15) : new Color(0, 0, 0, 0)));
                    g2.setColor(haloColor);
                    g2.fillOval(vx, vy, vd, vd);
                    g2.setColor(new Color(haloColor.getRed(), haloColor.getGreen(), haloColor.getBlue(), 50));
                    g2.drawOval(vx, vy, vd, vd);
                }
            }
        }

        // 3. Render all entities polymorphically
        for (Entity entity : grid.getAllEntities()) {
            if (entity.isAlive()) {
                entity.render(g2);
            }
        }
    }
}
