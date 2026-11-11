package com.ecosystem.ui;

import com.ecosystem.core.Engine;
import com.ecosystem.events.EcosystemEvent;
import com.ecosystem.events.EcosystemEventBus;
import com.ecosystem.grid.SpatialGrid;
import com.ecosystem.model.Entity;
import com.ecosystem.model.entities.*;
import com.ecosystem.util.EcosystemSummary;

import javax.swing.JPanel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * HUD panel displaying live ecological metrics, trophic counts, Shannon Diversity Index,
 * real-time event ticker subscribed to the Observer event bus, and State/Decorator legend.
 */
public final class StatisticsHUD extends JPanel {
    private static final long serialVersionUID = 1L;
    private final transient Engine engine;
    private final transient List<String> recentEventLogs = new ArrayList<>();

    public StatisticsHUD(Engine engine) {
        this.engine = engine;
        setPreferredSize(new Dimension(340, (int) engine.getConfig().worldHeight()));
        setBackground(new Color(20, 24, 28));

        // Register as Observer to receive real-time decoupled simulation events
        EcosystemEventBus.getInstance().subscribe(this::handleEcosystemEvent);
    }

    public synchronized void clearEvents() {
        recentEventLogs.clear();
    }

    private synchronized void handleEcosystemEvent(EcosystemEvent event) {
        long seconds = event.tick() / 60;
        String logEntry = String.format("[T+%03ds] %s", seconds, event.message());
        recentEventLogs.add(0, logEntry);
        if (recentEventLogs.size() > 8) {
            recentEventLogs.remove(recentEventLogs.size() - 1);
        }
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

        EcosystemSummary summary = engine.getLatestSummary();

        int y = 24;
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2.drawString("Ecosystem Dashboard", 15, y);

        y += 22;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.setColor(new Color(180, 190, 200));
        g2.drawString(String.format("Time: %.1fs  |  Tick: %d  |  %s",
                engine.getTotalSimulatedTime(),
                engine.getTotalTicks(),
                engine.isRunning() ? "RUNNING" : "PAUSED"), 15, y);

        y += 18;
        g2.drawString(String.format("Max Pedigree Generation: Gen %d", engine.getHighestGeneration()), 15, y);

        y += 18;
        g2.setColor(new Color(130, 220, 150));
        double shannon = (summary != null) ? summary.shannonDiversityIndex() : 0.0;
        g2.drawString(String.format("Shannon Diversity (H'): %.3f", shannon), 15, y);

        y += 18;
        g2.setColor(new Color(180, 200, 220));
        double meanSpeed = (summary != null) ? summary.averageSpeed() : 0.0;
        double meanVision = (summary != null) ? summary.averageVisionRadius() : 0.0;
        g2.drawString(String.format("Mean Speed: %.1f | Vision: %.1f", meanSpeed, meanVision), 15, y);

        y += 26;
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        drawMetricRow(g2, "Plants (Autotrophs):", plants, new Color(34, 177, 76), 15, y);
        y += 20;
        drawMetricRow(g2, "Herbivores (Prey):", herbivores, new Color(50, 150, 250), 15, y);
        y += 20;
        drawMetricRow(g2, "Carnivores (Predators):", carnivores, new Color(220, 50, 50), 15, y);
        y += 20;
        drawMetricRow(g2, "Apex Predators:", apex, new Color(140, 20, 180), 15, y);
        y += 20;
        drawMetricRow(g2, "Decomposers:", decomposers, new Color(200, 160, 40), 15, y);
        y += 20;
        drawMetricRow(g2, "Carcasses (Biomass):", carcasses, new Color(139, 69, 19), 15, y);

        // Draw Real-time Population Graph
        y += 24;
        drawPopulationGraph(g2, 15, y, getWidth() - 30, 175);

        // Draw Observer Event Log Ticker
        y += 188;
        drawEventTicker(g2, 15, y, getWidth() - 30, 150);

        // Draw State & Decorator Legend at bottom
        y += 162;
        drawLegend(g2, 15, y);
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
        g2.drawString("Population History (Lotka-Volterra)", x + 8, y + 16);

        List<Integer> plants = engine.getPlantHistory();
        List<Integer> herbivores = engine.getHerbivoreHistory();
        List<Integer> carnivores = engine.getCarnivoreHistory();
        List<Integer> apex = engine.getApexHistory();

        int points = plants.size();
        if (points < 2) return;

        int maxVal = 50;
        for (int i = 0; i < points; i++) {
            maxVal = Math.max(maxVal, plants.get(i));
            maxVal = Math.max(maxVal, herbivores.get(i));
            maxVal = Math.max(maxVal, carnivores.get(i));
            maxVal = Math.max(maxVal, apex.get(i));
        }

        drawLineSeries(g2, plants, new Color(34, 177, 76), x, y + 20, w, h - 25, maxVal);
        drawLineSeries(g2, herbivores, new Color(50, 150, 250), x, y + 20, w, h - 25, maxVal);
        drawLineSeries(g2, carnivores, new Color(220, 50, 50), x, y + 20, w, h - 25, maxVal);
        drawLineSeries(g2, apex, new Color(140, 20, 180), x, y + 20, w, h - 25, maxVal);
    }

    private static void drawLineSeries(Graphics2D g2, List<Integer> series, Color color, int x, int y, int w, int h, int maxVal) {
        g2.setColor(color);
        int n = series.size();
        for (int i = 0; i < n - 1; i++) {
            int x1 = x + (i * w) / (n - 1);
            int y1 = y + h - (series.get(i) * h) / maxVal;
            int x2 = x + ((i + 1) * w) / (n - 1);
            int y2 = y + h - (series.get(i + 1) * h) / maxVal;
            g2.drawLine(x1, y1, x2, y2);
        }
    }

    private synchronized void drawEventTicker(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(new Color(10, 14, 18));
        g2.fillRect(x, y, w, h);
        g2.setColor(new Color(50, 60, 70));
        g2.drawRect(x, y, w, h);

        Shape oldClip = g2.getClip();
        g2.setClip(x + 1, y + 1, w - 2, h - 2);

        g2.setColor(new Color(255, 180, 50));
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2.drawString("Observer Event Bus Ticker", x + 8, y + 16);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g2.setColor(new Color(200, 210, 220));

        int logY = y + 34;
        for (String log : recentEventLogs) {
            if (logY > y + h - 6) break;
            String display = log.length() > 46 ? log.substring(0, 43) + "..." : log;
            g2.drawString(display, x + 8, logY);
            logY += 15;
        }

        g2.setClip(oldClip);
    }

    private static void drawLegend(Graphics2D g2, int x, int y) {
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2.setColor(new Color(150, 160, 170));
        g2.drawString("States & Decorators:", x, y);

        int rowY = y + 14;
        drawLegendDot(g2, "Wander", new Color(100, 200, 255), x, rowY);
        drawLegendDot(g2, "Forage", new Color(255, 200, 50), x + 78, rowY);
        drawLegendDot(g2, "Flee", new Color(255, 60, 60), x + 150, rowY);
        drawLegendDot(g2, "Mate", new Color(255, 105, 180), x + 215, rowY);

        rowY += 15;
        drawLegendDot(g2, "Rest", new Color(160, 160, 160), x, rowY);
        drawLegendDot(g2, "Infected", new Color(110, 240, 40), x + 78, rowY);
        drawLegendDot(g2, "Camo", new Color(100, 230, 140), x + 150, rowY);
        drawLegendDot(g2, "Adren.", new Color(255, 140, 0), x + 215, rowY);
    }

    private static void drawLegendDot(Graphics2D g2, String label, Color color, int x, int y) {
        g2.setColor(color);
        g2.fillOval(x, y - 7, 7, 7);
        g2.setColor(new Color(195, 205, 215));
        g2.drawString(label, x + 11, y);
    }
}
