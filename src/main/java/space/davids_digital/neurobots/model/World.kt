package space.davids_digital.neurobots.model;

import java.util.ArrayList;
import java.util.List;

public class World {
    private int width;
    private int height;

    private final List<Wall> walls = new ArrayList<>();
    private final List<Creature> creatures = new ArrayList<>();
    private final List<Bullet> bullets = new ArrayList<>();

    public World(int width, int height) {
        setWidth(width);
        setHeight(height);
    }

    public void update(double delta) {
        creatures.forEach(c -> c.update(this, delta));
    }

    public List<Creature> getCreatures() {
        return creatures;
    }

    public List<Wall> getWalls() {
        return walls;
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
