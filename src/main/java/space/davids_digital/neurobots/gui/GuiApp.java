package space.davids_digital.neurobots.gui;

import space.davids_digital.neurobots.model.Creature;
import space.davids_digital.neurobots.model.NeuralNetwork;
import space.davids_digital.neurobots.model.Wall;
import space.davids_digital.neurobots.model.World;

import javax.swing.*;
import java.awt.*;

public class GuiApp {
    public static void main(String[] args) {
        new GuiApp();
    }

    private WorldViewer worldViewer;

    private World world;
    private JFrame frame;

    private GuiApp() {
        initWorld();
        initGui();
        initUpdating();
        initRendering();
    }

    private void initGui() {
        frame = new JFrame("Neurobots");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1200, 800));
        frame.setVisible(true);

        worldViewer = new WorldViewer(world);
        frame.getContentPane().add(worldViewer);
        worldViewer.grabFocus();
    }

    private void initWorld() {
        world = new World(1200, 800);
        world.getWalls().add(new Wall(new Point(0, 0), new Point(0, world.getHeight())));
        world.getWalls().add(new Wall(new Point(0, world.getHeight()), new Point(world.getWidth(), world.getHeight())));
        world.getWalls().add(new Wall(new Point(world.getWidth(), world.getHeight()), new Point(world.getWidth(), 0)));
        world.getWalls().add(new Wall(new Point(world.getWidth(), 0), new Point(0, 0)));
        world.getCreatures().add(new Creature(
                new NeuralNetwork(11, 1, 11, 3, -1, 1),
                Color.green,
                new Point(40, 40),
                100, 10, 0, 3, 12, 12, 30, Math.toRadians(45)
        ));
    }

    private void initUpdating() {
        var ref = new Object() {
            double lastUpdate = System.currentTimeMillis();
        };
        Timer timer = new Timer(10, event -> {
            double delta = System.currentTimeMillis() - ref.lastUpdate;
            ref.lastUpdate = System.currentTimeMillis();
            world.update(delta);
            worldViewer.update(delta);
        });
        timer.setRepeats(true);
        timer.start();
    }

    private void initRendering() {
        Timer timer = new Timer(10, event -> frame.repaint());
        timer.setRepeats(true);
        timer.start();
    }
}
