package com.ecosystem.ui;

import com.ecosystem.core.Engine;

import javax.swing.*;
import java.awt.*;

/**
 * Main application window (JFrame) containing simulation canvas, control toolbar,
 * and live dashboard. Runs the 60 FPS animation timer.
 */
public final class SimulationFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private final transient SimulationPanel simulationPanel;
    private final transient StatisticsHUD statisticsHUD;
    private final transient ControlPanel controlPanel;
    private final transient Timer animationTimer;

    public SimulationFrame(Engine engine) {
        super("COMP2000 - Multi-Tier Ecosystem & Evolution Simulation");

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        this.simulationPanel = new SimulationPanel(engine);
        this.statisticsHUD = new StatisticsHUD(engine);
        this.controlPanel = new ControlPanel(engine, simulationPanel, statisticsHUD);
        this.simulationPanel.setOnRefresh(statisticsHUD::repaint);

        add(controlPanel, BorderLayout.NORTH);
        add(simulationPanel, BorderLayout.CENTER);
        add(statisticsHUD, BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);
        setResizable(false);

        // 60 FPS Swing Timer for continuous updating and rendering
        this.animationTimer = new Timer(16, e -> {
            engine.update(0.016);
            simulationPanel.repaint();
            statisticsHUD.repaint();
        });
    }

    public void startSimulation() {
        setVisible(true);
        animationTimer.start();
    }
}
