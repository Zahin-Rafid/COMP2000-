package com.ecosystem.ui;

import com.ecosystem.core.Engine;
import com.ecosystem.grid.SpatialGrid;
import com.ecosystem.model.Entity;
import com.ecosystem.model.entities.*;

import javax.swing.JPanel;
import java.awt.*;
import java.util.List;

/**
 * HUD panel displaying live ecological metrics, trophic counts, and a real-time line graph.
 */
public class StatisticsHUD extends JPanel {
    private final Engine engine;

    public StatisticsHUD(Engine engine) {
        this.engine = engine;
        setPreferredSize(new Dimension(320, (int) engine.getConfig().worldHeight()));
        setBackground(new Color(20, 24, 28));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        SpatialGrid<Entity> grid = engine.getGrid();
        int plants = grid.queryEntities(Plant.class).size();
        int herbivores = grid.queryEntities(Herbivore.class).size();
        int carnivores = grid.queryEntities(Carnivore.class).size();
        int apex = grid.queryEntities(ApexPredator.class).size();
        int decomposers = grid.queryEntities(Decomposer.class).size();
        int carcasses = grid.queryEntities(Carcass.class).size();

        int y = 25;
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2.drawString("Ecosystem Dashboard", 15, y);

        y += 25;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.setColor(new Color(180, 190, 200));
        g2.drawString(String.format("Time: %.1fs  |  Tick: %d", engine.getTotalSimulatedTime(), engine.getTotalTicks()), 15, y);

        y += 18;
        g2.drawString(String.format("Max Generation: Gen %d", engine.getHighestGeneration()), 15, y);

        y += 28;
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        drawMetricRow(g2, "Plants (Autotrophs):", plants, new Color(34, 177, 76), 15, y);
        y += 22;
        drawMetricRow(g2, "Herbivores (Prey):", herbivores, new Color(50, 150, 250), 15, y);
        y += 22;
        drawMetricRow(g2, "Carnivores (Predators):", carnivores, new Color(220, 50, 50), 15, y);
        y += 22;
        drawMetricRow(g2, "Apex Predators:", apex, new Color(140, 20, 180), 15, y);
        y += 22;
        drawMetricRow(g2, "Decomposers:", decomposers, new Color(200, 160, 40), 15, y);
        y += 22;
        drawMetricRow(g2, "Carcasses (Biomass):", carcasses, new Color(139, 69, 19), 15, y);

        // Draw Real-time Population Graph
        y += 35;
        drawPopulationGraph(g2, 15, y, getWidth() - 30, 220);
    }

    private void drawMetricRow(Graphics2D g2, String label, int count, Color color, int x, int y) {
        g2.setColor(color);
        g2.fillOval(x, y - 10, 10, 10);
        g2.setColor(new Color(220, 225, 230));
        g2.drawString(label, x + 16, y);
        g2.setColor(Color.WHITE);
        g2.drawString(String.valueOf(count), getWidth() - 50, y);
    }

    private void drawPopulationGraph(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(new Color(10, 14, 18));
        g2.fillRect(x, y, w, h);
        g2.setColor(new Color(50, 60, 70));
        g2.drawRect(x, y, w, h);

        g2.setColor(new Color(150, 160, 170));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.drawString("Population History", x + 8, y + 16);

        List<Integer> plants = engine.getPlantHistory();
        List<Integer> herbivores = engine.getHerbivoreHistory();
        List<Integer> carnivores = engine.getCarnivoreHistory();
        List<Integer> apex = engine.getApexHistory();

        int points = plants.size();
        if (points < 2) return;

        // Find max scale
        int maxVal = 50;
        for (int i = 0; i < points; i++) {
            maxVal = Math.max(maxVal, plants.get(i));
            maxVal = Math.max(maxVal, herbivores.get(i));
            maxVal = Math.max(maxVal, carnivores.get(i));
            maxVal = Math.max(maxVal, apex.get(i));
        }

        drawGraphLine(g2, plants, maxVal, new Color(34, 177, 76), x, y, w, h);
        drawGraphLine(g2, herbivores, maxVal, new Color(50, 150, 250), x, y, w, h);
        drawGraphLine(g2, carnivores, maxVal, new Color(220, 50, 50), x, y, w, h);
        drawGraphLine(g2, apex, maxVal, new Color(140, 20, 180), x, y, w, h);
    }

    private void drawGraphLine(Graphics2D g2, List<Integer> history, int maxVal, Color color, int x, int y, int w, int h) {
        g2.setColor(color);
        int n = history.size();
        for (int i = 0; i < n - 1; i++) {
            int x1 = x + (i * w) / Math.max(1, n - 1);
            int y1 = y + h - (int) ((history.get(i) / (double) maxVal) * (h - 25));
            int x2 = x + ((i + 1) * w) / Math.max(1, n - 1);
            int y2 = y + h - (int) ((history.get(i + 1) / (double) maxVal) * (h - 25));
            g2.drawLine(x1, y1, x2, y2);
        }
    }
}
