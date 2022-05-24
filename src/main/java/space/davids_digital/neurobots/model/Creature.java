package space.davids_digital.neurobots.model;

import java.awt.*;

public class Creature {
    private Color color;
    private Point position;
    private double visionDistance;
    private double rotation;
    private double fitness;
    private double health;
    private double radius;
    private double fov;

    public Creature(
            Color color,
            Point position,
            double visionDistance,
            double rotation,
            double fitness,
            double health,
            double radius,
            double fov
    ) {
        this.color = color;
        this.position = position;
        this.visionDistance = visionDistance;
        this.rotation = rotation;
        this.fitness = fitness;
        this.health = health;
        this.radius = radius;
        this.fov = fov;
    }

    public double getVisionDistance() {
        return visionDistance;
    }

    public void setVisionDistance(double visionDistance) {
        this.visionDistance = visionDistance;
    }

    public double getFov() {
        return fov;
    }

    public void setFov(double fov) {
        this.fov = fov;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getHealth() {
        return health;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public double getRotation() {
        return rotation;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
