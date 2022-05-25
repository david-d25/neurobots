package space.davids_digital.neurobots.gui;

import space.davids_digital.neurobots.model.Creature;
import space.davids_digital.neurobots.model.NeuralNetwork;
import space.davids_digital.neurobots.model.World;

import javax.swing.*;
import java.awt.*;

public class GuiApp {
    public static void main(String[] args) {
        new GuiApp();
    }

    private WorldViewer worldViewer;

    private World world;

    private GuiApp() {
        initWorld();
        initGui();
        initUpdating();
        initRendering();
    }

    private void initGui() {
        JFrame frame = new JFrame("Neurobots");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(680, 400));
        frame.setVisible(true);

        worldViewer = new WorldViewer(world);
        frame.getContentPane().add(worldViewer);
    }

    private void initWorld() {
        world = new World(1200, 800);
        world.addCreature(new Creature(
                new NeuralNetwork(12, 1, 12, 3, -1, 1),
                Color.green,
                new Point(100, 100),
                100, 3, 23, 3, 12, 30, 45
        ));
    }

    private void initUpdating() {
        new Thread(() -> {
            double lastUpdate = System.currentTimeMillis();
            while (true) {
                double delta = System.currentTimeMillis() - lastUpdate;
                lastUpdate = System.currentTimeMillis();
                world.update(delta);
            }
        }).start();
    }

    private void initRendering() {
        new Thread(worldViewer::repaint).start();
    }
}
