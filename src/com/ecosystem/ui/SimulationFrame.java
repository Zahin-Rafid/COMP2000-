package com.ecosystem.ui;

import com.ecosystem.core.Engine;

import javax.swing.*;
import java.awt.*;

/**
 * Main application window (JFrame) containing simulation canvas, control toolbar,
 * and live dashboard. Runs the 60 FPS animation timer.
 */
public class SimulationFrame extends JFrame {
    private final SimulationPanel simulationPanel;
    private final StatisticsHUD statisticsHUD;
    private final ControlPanel controlPanel;
    private final Timer animationTimer;

    public SimulationFrame(Engine engine) {
        super("COMP2000 - Multi-Tier Ecosystem & Evolution Simulation");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        this.simulationPanel = new SimulationPanel(engine);
        this.statisticsHUD = new StatisticsHUD(engine);
        this.controlPanel = new ControlPanel(engine, simulationPanel);

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
