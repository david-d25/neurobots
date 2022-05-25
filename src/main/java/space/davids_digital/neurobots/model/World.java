package space.davids_digital.neurobots.model;

import java.util.HashSet;
import java.util.Set;

public class World {
    private int width;
    private int height;

    private Set<Wall> walls = new HashSet<>();
    private Set<Creature> creatures = new HashSet<>();
    private Set<Bullet> bullets = new HashSet<>();

    public World(int width, int height) {
        setWidth(width);
        setHeight(height);
    }

    public void update(double delta) {

    }

    public void addCreature(Creature creature) {
        creatures.add(creature);
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
