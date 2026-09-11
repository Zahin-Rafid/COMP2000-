package com.ecosystem.ui;

import com.ecosystem.core.Engine;

import javax.swing.*;
import java.awt.*;

/**
 * Control toolbar hosting user interaction widgets, speed sliders, and brush selectors.
 */
public class ControlPanel extends JPanel {
    public ControlPanel(Engine engine, SimulationPanel simulationPanel) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));
        setBackground(new Color(30, 36, 42));

        // 1. Play / Pause Button
        JButton pauseBtn = new JButton("Pause");
        pauseBtn.addActionListener(e -> {
            engine.toggleRunning();
            pauseBtn.setText(engine.isRunning() ? "Pause" : "Resume");
        });
        add(pauseBtn);

        // 2. Single Step Button
        JButton stepBtn = new JButton("Step");
        stepBtn.addActionListener(e -> {
            if (!engine.isRunning()) {
                engine.update(0.05);
                simulationPanel.repaint();
            }
        });
        add(stepBtn);

        // 3. Reset Button
        JButton resetBtn = new JButton("Reset");
        resetBtn.addActionListener(e -> {
            engine.reset();
            pauseBtn.setText("Pause");
            simulationPanel.repaint();
        });
        add(resetBtn);

        // 4. Speed Slider
        JLabel speedLabel = new JLabel("Speed: 1.0x");
        speedLabel.setForeground(Color.WHITE);
        JSlider speedSlider = new JSlider(1, 50, 10);
        speedSlider.setPreferredSize(new Dimension(120, 25));
        speedSlider.setBackground(new Color(30, 36, 42));
        speedSlider.addChangeListener(e -> {
            double multiplier = speedSlider.getValue() / 10.0;
            engine.setSpeedMultiplier(multiplier);
            speedLabel.setText(String.format("Speed: %.1fx", multiplier));
        });
        add(speedLabel);
        add(speedSlider);

        // 5. Spawn Tool Brush
        JLabel brushLabel = new JLabel("Click Tool:");
        brushLabel.setForeground(Color.WHITE);
        String[] tools = {"Plant", "Herbivore", "Carnivore", "Apex Predator", "Decomposer", "Carcass"};
        JComboBox<String> toolBox = new JComboBox<>(tools);
        toolBox.addActionListener(e -> simulationPanel.setSelectedSpawnTool((String) toolBox.getSelectedItem()));
        add(brushLabel);
        add(toolBox);

        // 6. View Toggles
        JCheckBox visionCheck = new JCheckBox("Vision Cones");
        visionCheck.setForeground(Color.WHITE);
        visionCheck.setBackground(new Color(30, 36, 42));
        visionCheck.addActionListener(e -> {
            simulationPanel.setShowVisionHalos(visionCheck.isSelected());
            simulationPanel.repaint();
        });
        add(visionCheck);

        JCheckBox heatmapCheck = new JCheckBox("Soil Nutrients");
        heatmapCheck.setForeground(Color.WHITE);
        heatmapCheck.setBackground(new Color(30, 36, 42));
        heatmapCheck.addActionListener(e -> {
            simulationPanel.setShowNutrientHeatmap(heatmapCheck.isSelected());
            simulationPanel.repaint();
        });
        add(heatmapCheck);
    }
}
