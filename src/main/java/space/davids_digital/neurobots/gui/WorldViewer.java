package space.davids_digital.neurobots.gui;

import space.davids_digital.neurobots.model.World;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

public class WorldViewer extends JPanel {
    private World world;
    private double cameraX = 0;
    private double cameraY = 0;
    private double viewportWidth = 1200;
    private double viewportHeight = 800;

    public WorldViewer(World world) {
        this.world = world;
    }

    @Override
    public void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;
        g.setColor(Color.GRAY);
        g.fillRect(0, 0, getWidth(), getHeight());

//        g.setTransform(AffineTransform.getTranslateInstance(-getWidth()/2.0, -getHeight()/2.0));
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, world.getWidth(), world.getHeight());
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }
}
