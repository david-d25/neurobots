package space.davids_digital.neurobots.gui;

import space.davids_digital.neurobots.model.World;

import javax.swing.*;
import java.awt.*;

public class GuiApp {
    public static void main(String[] args) {
        new GuiApp();
    }

    private JFrame frame;
    private WorldViewer worldViewer;

    private World world;

    private GuiApp() {
        initWorld();
        initGui();
    }

    private void initGui() {
        frame = new JFrame("Neurobots");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(680, 400));
        frame.setVisible(true);

        worldViewer = new WorldViewer(world);
        frame.getContentPane().add(worldViewer);
    }

    private void initWorld() {
        world = new World(1200, 800);
    }
}
