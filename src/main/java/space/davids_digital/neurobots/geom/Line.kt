package space.davids_digital.neurobots.geom;

import java.awt.*;

public class Line {
    private DoublePoint pointA;
    private DoublePoint pointB;

    public Line(DoublePoint pointA, DoublePoint pointB) {
        this.pointA = pointA;
        this.pointB = pointB;
    }

    public Line(Point pointA, Point pointB) {
        this.pointA = new DoublePoint(pointA.x, pointA.y);
        this.pointB = new DoublePoint(pointB.x, pointB.y);
    }

    public DoublePoint getPointA() {
        return pointA;
    }

    public void setPointA(DoublePoint pointA) {
        this.pointA = pointA;
    }

    public DoublePoint getPointB() {
        return pointB;
    }

    public void setPointB(DoublePoint pointB) {
        this.pointB = pointB;
    }
}
