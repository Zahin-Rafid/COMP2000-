package com.ecosystem.ui;

import com.ecosystem.core.Engine;
import com.ecosystem.core.Vector2D;
import com.ecosystem.events.EcosystemEvent;
import com.ecosystem.events.EcosystemEventBus;
import com.ecosystem.events.EcosystemEventType;
import com.ecosystem.exceptions.EntityOutOfBoundsException;
import com.ecosystem.grid.Cell;
import com.ecosystem.grid.SpatialGrid;
import com.ecosystem.model.Entity;
import com.ecosystem.model.Organism;
import com.ecosystem.model.decorator.OrganismDecorator;
import com.ecosystem.model.entities.*;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Custom JPanel drawing the 2D ecological terrain, entity layers, vision ranges,
 * disaster visual overlays, and interactive click/drag spawning.
 */
public class SimulationPanel extends JPanel {
    private final Engine engine;
    private Runnable onRefresh;
    private boolean showVisionHalos = false;
    private boolean showNutrientHeatmap = false;
    private String selectedSpawnTool = "Plant";

    private Vector2D lastDragPos = null;
    private volatile Vector2D activeEffectPos = null;
    private volatile Color activeEffectColor = null;
    private volatile long effectExpireTimeMs = 0L;

    public SimulationPanel(Engine engine) {
        this.engine = engine;
        setPreferredSize(new Dimension((int) engine.getConfig().worldWidth(), (int) engine.getConfig().worldHeight()));
        setBackground(new Color(25, 30, 35));

        setupMouseInteractions();
        setupDisasterVisuals();
    }

    public void setOnRefresh(Runnable onRefresh) {
        this.onRefresh = onRefresh;
    }

    private void setupDisasterVisuals() {
        EcosystemEventBus.getInstance().subscribe(this::onEvent);
    }

    private void onEvent(EcosystemEvent event) {
        if (event.type() == EcosystemEventType.ENVIRONMENTAL_DISASTER && event.location() != null) {
            boolean isFire = event.message().toLowerCase().contains("wildfire");
            activeEffectPos = event.location();
            activeEffectColor = isFire ? new Color(255, 80, 20, 110) : new Color(210, 160, 50, 70);
            effectExpireTimeMs = System.currentTimeMillis() + 1200;
        } else if (event.type() == EcosystemEventType.FERTILE_BLOOM) {
            activeEffectPos = event.location();
            activeEffectColor = new Color(50, 220, 100, 60);
            effectExpireTimeMs = System.currentTimeMillis() + 1000;
        }
    }

    private void setupMouseInteractions() {
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Vector2D pos = new Vector2D(e.getX(), e.getY());
                lastDragPos = pos;
                spawnEntityAt(pos);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                Vector2D pos = new Vector2D(e.getX(), e.getY());
                if (lastDragPos == null || lastDragPos.distanceTo(pos) >= 16.0) {
                    lastDragPos = pos;
                    spawnEntityAt(pos);
                }
            }
        };
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    private void spawnEntityAt(Vector2D pos) {
        SpatialGrid<Entity> grid = engine.getGrid();
        if (!grid.isWithinBounds(pos)) return;

        try {
            Entity spawned = switch (selectedSpawnTool) {
                case "Plant" -> Plant.createDefault(pos);
                case "Herbivore" -> Herbivore.createDefault(pos);
                case "Carnivore" -> Carnivore.createDefault(pos);
                case "Apex Predator" -> ApexPredator.createDefault(pos);
                case "Decomposer" -> Decomposer.createDefault(pos);
                case "Carcass" -> new Carcass(pos, 30.0);
                default -> null;
            };
            if (spawned != null) {
                engine.spawnEntity(spawned);
                repaint();
                if (onRefresh != null) {
                    onRefresh.run();
                }
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
                    Organism base = (org instanceof OrganismDecorator dec) ? dec.unwrap() : org;
                    double vision = base.getGenome().getVisionRadius();
                    int vx = (int) (base.getPosition().getX() - vision);
                    int vy = (int) (base.getPosition().getY() - vision);
                    int vd = (int) (vision * 2);

                    Color haloColor = base instanceof ApexPredator ? new Color(140, 20, 180, 22) :
                                     (base instanceof Carnivore ? new Color(220, 50, 50, 22) :
                                     (base instanceof Herbivore ? new Color(50, 150, 250, 18) : new Color(0, 0, 0, 0)));
                    if (haloColor.getAlpha() > 0) {
                        g2.setColor(haloColor);
                        g2.fillOval(vx, vy, vd, vd);
                        g2.setColor(new Color(haloColor.getRed(), haloColor.getGreen(), haloColor.getBlue(), 65));
                        g2.drawOval(vx, vy, vd, vd);
                    }
                }
            }
        }

        // 3. Render all entities polymorphically
        for (Entity entity : grid.getAllEntities()) {
            if (entity.isAlive()) {
                entity.render(g2);
            }
        }

        // 4. Render active disaster / bloom visual overlay if recent
        if (activeEffectPos != null && activeEffectColor != null && System.currentTimeMillis() < effectExpireTimeMs) {
            g2.setColor(activeEffectColor);
            if (activeEffectColor.getRed() > 230) {
                int r = 135;
                g2.fillOval((int) activeEffectPos.getX() - r, (int) activeEffectPos.getY() - r, r * 2, r * 2);
                g2.setColor(new Color(255, 160, 40, 200));
                g2.drawOval((int) activeEffectPos.getX() - r, (int) activeEffectPos.getY() - r, r * 2, r * 2);
            } else {
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }
}
