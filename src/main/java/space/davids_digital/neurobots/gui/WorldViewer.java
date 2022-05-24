package space.davids_digital.neurobots.gui;

import space.davids_digital.neurobots.model.World;

import javax.swing.*;
import java.awt.*;

public class WorldViewer extends JPanel {
    private World world;

    public WorldViewer(World world) {
        this.world = world;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(Color.YELLOW);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }
}
