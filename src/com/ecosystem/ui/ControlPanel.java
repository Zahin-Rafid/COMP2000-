package com.ecosystem.ui;

import com.ecosystem.core.Engine;
import com.ecosystem.core.Vector2D;
import com.ecosystem.model.Organism;
import com.ecosystem.model.decorator.InfectedDebuff;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Control toolbar hosting user interaction widgets, speed sliders, brush selectors,
 * and environmental disaster triggers.
 */
public class ControlPanel extends JPanel {
    public ControlPanel(Engine engine, SimulationPanel simulationPanel) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 6));
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
        speedSlider.setPreferredSize(new Dimension(100, 24));
        speedSlider.setBackground(new Color(30, 36, 42));
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
        JButton fireBtn = new JButton("Wildfire");
        fireBtn.setBackground(new Color(180, 50, 30));
        fireBtn.setForeground(Color.WHITE);
        fireBtn.addActionListener(e -> {
            double cx = engine.getConfig().worldWidth() / 2.0;
            double cy = engine.getConfig().worldHeight() / 2.0;
            engine.triggerWildfire(new Vector2D(cx, cy), 130.0);
            simulationPanel.repaint();
        });
        add(fireBtn);

        JButton droughtBtn = new JButton("Drought");
        droughtBtn.setBackground(new Color(180, 130, 30));
        droughtBtn.setForeground(Color.WHITE);
        droughtBtn.addActionListener(e -> {
            engine.triggerDrought(0.6);
            simulationPanel.repaint();
        });
        add(droughtBtn);

        JButton bloomBtn = new JButton("Bloom");
        bloomBtn.setBackground(new Color(40, 150, 60));
        bloomBtn.setForeground(Color.WHITE);
        bloomBtn.addActionListener(e -> {
            engine.triggerFertileBloom();
            simulationPanel.repaint();
        });
        add(bloomBtn);

        // 7. Decorator Pattern Trigger (Infect Random Organism)
        JButton infectBtn = new JButton("Infect");
        infectBtn.setBackground(new Color(80, 140, 40));
        infectBtn.setForeground(Color.WHITE);
        infectBtn.addActionListener(e -> {
            List<Organism> organisms = engine.getGrid().queryEntities(Organism.class);
            if (!organisms.isEmpty()) {
                Organism target = organisms.get((int) (Math.random() * organisms.size()));
                if (!(target instanceof InfectedDebuff)) {
                    engine.getGrid().removeEntity(target);
                    try {
                        engine.getGrid().addEntity(new InfectedDebuff(target));
                    } catch (Exception ignored) {}
                }
            }
        });
        add(infectBtn);

        // 8. View Toggles
        JCheckBox heatmapCheck = new JCheckBox("Nutrients");
        heatmapCheck.setForeground(Color.WHITE);
        heatmapCheck.setBackground(new Color(30, 36, 42));
        heatmapCheck.addActionListener(e -> {
            simulationPanel.setShowNutrientHeatmap(heatmapCheck.isSelected());
            simulationPanel.repaint();
        });
        add(heatmapCheck);
    }
}
