package space.davids_digital.neurobots.model;

import java.awt.*;

public class Bullet {
    private Point position;
    private Point speed;
    private Creature firedBy;
    private double damage;

    public Bullet(Creature firedBy, double damage, Point position, Point speed) {
        setFiredBy(firedBy);
        setDamage(damage);
        setPosition(position);
        setSpeed(speed);
    }

    public Creature getFiredBy() {
        return firedBy;
    }

    public void setFiredBy(Creature firedBy) {
        this.firedBy = firedBy;
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public Point getSpeed() {
        return speed;
    }

    public void setSpeed(Point speed) {
        this.speed = speed;
    }
}
