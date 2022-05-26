package space.davids_digital.neurobots.model;

import space.davids_digital.neurobots.geom.DoublePoint;
import space.davids_digital.neurobots.geom.GeometryUtils;
import space.davids_digital.neurobots.geom.Line;

import java.awt.*;
import java.util.Arrays;
import java.util.Set;

import static java.lang.Math.*;

public class Creature {
    private NeuralNetwork neuralNetwork;
    private Color color;
    private Point position;
    private double visionDistance;
    private int raysNumber;
    private double angle;
    private double fitness;
    private double energy;
    private double maxEnergy;
    private double radius;
    private double fov;

    private final transient double[] wallRayData;

    public Creature(
            NeuralNetwork neuralNetwork,
            Color color,
            Point position,
            double visionDistance,
            int raysNumber,
            double angle,
            double fitness,
            double energy,
            double maxEnergy,
            double radius,
            double fov
    ) {
        this.neuralNetwork = neuralNetwork;
        this.color = color;
        this.position = position;
        this.visionDistance = visionDistance;
        this.raysNumber = raysNumber;
        this.angle = angle;
        this.fitness = fitness;
        this.energy = energy;
        this.maxEnergy = maxEnergy;
        this.radius = radius;
        this.fov = fov;

        wallRayData = new double[raysNumber];
    }

    public void update(World world, double delta) {
        double[] input = new double[raysNumber + 1];
        System.arraycopy(wallRayData, 0, input, 0, wallRayData.length);
        input[wallRayData.length] = energy/maxEnergy; // energy
        double[] output = this.neuralNetwork.getResponse(input);
        double forward = output[0] * delta / 10;
        double right = output[1] * delta / 10;
        double rotation = output[2] * delta / 100;
        setAngle(getAngle() + rotation);
        position.x += Math.cos(angle)*forward + Math.cos(angle+Math.PI/2)*right;
        position.y += Math.sin(angle)*forward + Math.sin(angle+Math.PI/2)*right;
        updateRayData(world);
    }

    private void updateRayData(World world) {
        double x = getPosition().getX();
        double y = getPosition().getY();

        Arrays.fill(wallRayData, 0);
        world.getWalls().forEach(wall -> {
            for (int i = 0; i < wallRayData.length; i++) {

                double rayAngle = getAngle() - getFov()/2 + getFov()/getRaysNumber()*(i + 0.5);
                Line rayLine = new Line(
                        new DoublePoint(x + cos(rayAngle)*getRadius(), y + sin(rayAngle)*getRadius()),
                        new DoublePoint(x + cos(rayAngle)*(getRadius() + getVisionDistance()), y + sin(rayAngle)*(getRadius() + getVisionDistance()))
                );

                Line wallLine = new Line(wall.getPointA(), wall.getPointB());

                Set<DoublePoint> intersections = GeometryUtils.intersections(wallLine, rayLine);
                if (intersections.isEmpty()) continue;
                DoublePoint intersection = intersections.stream().findFirst().get();

                double distance = sqrt(pow(rayLine.getPointA().getX() - intersection.getX(), 2) + pow(rayLine.getPointA().getY() - intersection.getY(), 2));
                double maxDistance = sqrt(pow(rayLine.getPointA().getX() - rayLine.getPointB().getX(), 2) + pow(rayLine.getPointA().getY() - rayLine.getPointB().getY(), 2));
                wallRayData[i] = max(wallRayData[i], 1 - distance/maxDistance);
            }
            // TODO enemy ray data
        });
    }

    public double[] getWallRayData() {
        return wallRayData;
    }

    public NeuralNetwork getNeuralNetwork() {
        return neuralNetwork;
    }

    public void setNeuralNetwork(NeuralNetwork neuralNetwork) {
        this.neuralNetwork = neuralNetwork;
    }

    public double getRaysNumber() {
        return raysNumber;
    }

    public void setRaysNumber(int raysNumber) {
        this.raysNumber = raysNumber;
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

    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
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
