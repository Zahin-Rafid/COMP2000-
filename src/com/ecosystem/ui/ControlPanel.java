package com.ecosystem.ui;

import com.ecosystem.core.Engine;
import com.ecosystem.core.Vector2D;

import javax.swing.*;
import java.awt.*;

/**
 * Control toolbar hosting user interaction widgets, speed sliders, brush selectors,
 * environmental disaster triggers, and decorator injectors.
 */
public final class ControlPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    public ControlPanel(Engine engine, SimulationPanel simulationPanel, StatisticsHUD statisticsHUD) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 6, 6));
        setBackground(new Color(30, 36, 42));

        Runnable refreshUi = () -> {
            simulationPanel.repaint();
            statisticsHUD.repaint();
        };

        // 1. Play / Pause Button
        JButton pauseBtn = createStyledButton("Pause", new Color(65, 80, 95));
        pauseBtn.addActionListener(e -> {
            engine.toggleRunning();
            pauseBtn.setText(engine.isRunning() ? "Pause" : "Resume");
            refreshUi.run();
        });
        add(pauseBtn);

        // 2. Single Step Button (works reliably when paused)
        JButton stepBtn = createStyledButton("Step", new Color(65, 80, 95));
        stepBtn.addActionListener(e -> {
            engine.stepOnce(0.05);
            refreshUi.run();
        });
        add(stepBtn);

        // 3. Reset Button
        JButton resetBtn = createStyledButton("Reset", new Color(65, 80, 95));
        resetBtn.addActionListener(e -> {
            statisticsHUD.clearEvents();
            engine.reset();
            pauseBtn.setText("Pause");
            refreshUi.run();
        });
        add(resetBtn);

        // 4. Speed Slider
        JLabel speedLabel = new JLabel("Speed: 1.0x");
        speedLabel.setForeground(Color.WHITE);
        JSlider speedSlider = new JSlider(1, 50, 10);
        speedSlider.setPreferredSize(new Dimension(90, 24));
        speedSlider.setOpaque(false);
        speedSlider.addChangeListener(e -> {
            double multiplier = speedSlider.getValue() / 10.0;
            engine.setSpeedMultiplier(multiplier);
            speedLabel.setText(String.format("Speed: %.1fx", multiplier));
        });
        add(speedLabel);
        add(speedSlider);

        // 5. Spawn Tool Brush
        JLabel brushLabel = new JLabel("Spawn:");
        brushLabel.setForeground(Color.WHITE);
        String[] tools = {"Plant", "Herbivore", "Carnivore", "Apex Predator", "Decomposer", "Carcass"};
        JComboBox<String> toolBox = new JComboBox<>(tools);
        toolBox.addActionListener(e -> simulationPanel.setSelectedSpawnTool((String) toolBox.getSelectedItem()));
        add(brushLabel);
        add(toolBox);

        // 6. Disaster Triggers (Emergent Behavior & Observer Testing)
        JButton fireBtn = createStyledButton("Wildfire", new Color(180, 50, 30));
        fireBtn.addActionListener(e -> {
            double cx = engine.getConfig().worldWidth() / 2.0;
            double cy = engine.getConfig().worldHeight() / 2.0;
            engine.triggerWildfire(new Vector2D(cx, cy), 140.0);
            refreshUi.run();
        });
        add(fireBtn);

        JButton droughtBtn = createStyledButton("Drought", new Color(175, 120, 25));
        droughtBtn.addActionListener(e -> {
            engine.triggerDrought(0.6);
            refreshUi.run();
        });
        add(droughtBtn);

        JButton bloomBtn = createStyledButton("Bloom", new Color(40, 145, 65));
        bloomBtn.addActionListener(e -> {
            engine.triggerFertileBloom();
            refreshUi.run();
        });
        add(bloomBtn);

        // 7. Decorator Pattern Triggers
        JButton infectBtn = createStyledButton("Infect", new Color(90, 140, 35));
        infectBtn.addActionListener(e -> {
            engine.infectRandomOrganism();
            refreshUi.run();
        });
        add(infectBtn);

        JButton buffBtn = createStyledButton("Trait Buff", new Color(120, 80, 175));
        buffBtn.addActionListener(e -> {
            engine.buffRandomOrganism();
            refreshUi.run();
        });
        add(buffBtn);

        // 8. View Toggles
        JCheckBox visionCheck = new JCheckBox("Vision");
        visionCheck.setForeground(Color.WHITE);
        visionCheck.setOpaque(false);
        visionCheck.setFocusPainted(false);
        visionCheck.addActionListener(e -> {
            simulationPanel.setShowVisionHalos(visionCheck.isSelected());
            simulationPanel.repaint();
        });
        add(visionCheck);

        JCheckBox heatmapCheck = new JCheckBox("Nutrients");
        heatmapCheck.setForeground(Color.WHITE);
        heatmapCheck.setOpaque(false);
        heatmapCheck.setFocusPainted(false);
        heatmapCheck.addActionListener(e -> {
            simulationPanel.setShowNutrientHeatmap(heatmapCheck.isSelected());
            simulationPanel.repaint();
        });
        add(heatmapCheck);
    }

    private static JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setMargin(new Insets(4, 9, 4, 9));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
