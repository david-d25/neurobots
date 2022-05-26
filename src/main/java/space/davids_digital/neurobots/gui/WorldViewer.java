package space.davids_digital.neurobots.gui;

import space.davids_digital.neurobots.model.World;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.util.HashSet;
import java.util.Set;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class WorldViewer extends JPanel implements KeyListener, MouseWheelListener {
    private World world;
    private double cameraX = 0;
    private double cameraY = 0;
    private double viewportHeight = 800;
    private final AffineTransform cameraTransform = new AffineTransform();
    private final Set<Integer> pressedKeys = new HashSet<>();

    public WorldViewer(World world) {
        this.world = world;
        addMouseWheelListener(this);
        addKeyListener(this);
        setFocusable(true);
        updateCameraTransform();
    }

    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D g = (Graphics2D) graphics;
        g.setColor(Color.GRAY);
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setTransform(cameraTransform);
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, world.getWidth(), world.getHeight());

        g.setStroke(new BasicStroke(2));

        g.setColor(Color.BLACK);
        world.getWalls().forEach(wall -> g.drawLine(
                wall.getPointA().x, wall.getPointA().y,
                wall.getPointB().x, wall.getPointB().y
        ));

        // Creature
        world.getCreatures().forEach(creature -> {
            int x = creature.getPosition().x;
            int y = creature.getPosition().y;
            double radius = creature.getRadius();
            double angle = creature.getAngle();

            g.setColor(creature.getColor());
            g.fillArc(x - (int) radius, y - (int) radius, (int) creature.getRadius()*2, (int) creature.getRadius()*2, 0, 360);
            g.setColor(creature.getColor().darker());
            g.drawArc(x - (int) radius, y - (int) radius, (int) creature.getRadius()*2, (int) creature.getRadius()*2, 0, 360);
            g.fillPolygon(
                    new int[] {
                            (int) (x + 0.5*radius*cos(angle + 0.8)),
                            (int) (x + 0.8*radius*cos(angle)),
                            (int) (x + 0.5*radius*cos(angle - 0.8))
                    },
                    new int[] {
                            (int) (y + 0.5*radius*sin(angle + 0.8)),
                            (int) (y + 0.8*radius*sin(angle)),
                            (int) (y + 0.5*radius*sin(angle - 0.8)),
                    },
                    3
            );

            // Rays
            for (int rayId = 0; rayId < creature.getRaysNumber(); rayId++) {
                double rayLength = creature.getWallRayData()[rayId];
                g.setColor(new Color(0, 0, 1, (float) (0.25 + 0.75 * rayLength)));
                double rayAngle = angle - creature.getFov() / 2 + creature.getFov() / creature.getRaysNumber() * (rayId + 0.5);
                g.drawLine(
                        (int) (x + cos(rayAngle) * radius),
                        (int) (y + sin(rayAngle) * radius),
                        (int) (x + cos(rayAngle) * (radius + creature.getVisionDistance() * (1 - rayLength))),
                        (int) (y + sin(rayAngle) * (radius + creature.getVisionDistance() * (1 - rayLength)))
                );
            }
        });
    }

    public void update(double delta) {
        if (pressedKeys.contains(KeyEvent.VK_RIGHT))
            moveCamera(delta*viewportHeight/1000, 0);
        if (pressedKeys.contains(KeyEvent.VK_LEFT))
            moveCamera(-delta*viewportHeight/1000, 0);
        if (pressedKeys.contains(KeyEvent.VK_DOWN))
            moveCamera(0, delta*viewportHeight/1000);
        if (pressedKeys.contains(KeyEvent.VK_UP))
            moveCamera(0, -delta*viewportHeight/1000);
    }

    public void moveCamera(double dx, double dy) {
        cameraX += dx;
        cameraY += dy;
        updateCameraTransform();
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        pressedKeys.add(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        pressedKeys.remove(e.getKeyCode());
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        viewportHeight += e.getPreciseWheelRotation()*100;
        updateCameraTransform();
    }

    private void updateCameraTransform() {
        double scale = getHeight()/viewportHeight;

        cameraTransform.setToIdentity();
        cameraTransform.translate(getWidth()/2.0, getHeight()/2.0);
        cameraTransform.scale(scale, scale);
        cameraTransform.translate(-cameraX*scale, -cameraY*scale);
    }
}
