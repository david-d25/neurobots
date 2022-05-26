package space.davids_digital.neurobots.geom;

import java.util.Collections;
import java.util.Set;

public class GeometryUtils {
    public static Set<DoublePoint> intersections(Line lineA, Line lineB) {
        double angle1Ratio = (lineA.getPointB().getY() - lineA.getPointA().getY()) / (lineA.getPointB().getX() - lineA.getPointA().getX());
        double angle2Ratio = (lineB.getPointB().getY() - lineB.getPointA().getY()) / (lineB.getPointB().getX() - lineB.getPointA().getX());

        if (    (Double.isNaN(angle1Ratio) || Double.isNaN(angle2Ratio)) ||
                (Double.isInfinite(angle1Ratio) && Double.isInfinite(angle2Ratio)) ||
                (angle1Ratio == angle2Ratio)
        ) return Collections.emptySet();

        if (Double.isInfinite(angle1Ratio))
            return verticalLineIntersection(lineA, lineB);

        if (Double.isInfinite(angle2Ratio))
            return verticalLineIntersection(lineB, lineA);

        double lineAOffset = lineA.getPointA().getY() - angle1Ratio * lineA.getPointA().getX();
        double lineBOffset = lineB.getPointA().getY() - angle2Ratio * lineB.getPointA().getX();

        DoublePoint intersection = new DoublePoint((lineBOffset - lineAOffset) / (angle1Ratio - angle2Ratio), 0);
        intersection.setY(angle1Ratio * intersection.getX() + lineAOffset);

        DoublePoint avgAPoint = new DoublePoint(
                (lineA.getPointA().getX() + lineA.getPointB().getX())/2,
                (lineA.getPointA().getY() + lineA.getPointB().getY())/2
        );
        DoublePoint lineARect = new DoublePoint(
                Math.abs(lineA.getPointA().getX() - lineA.getPointB().getX()),
                Math.abs(lineA.getPointA().getY() - lineA.getPointB().getY())
        );
        DoublePoint avgBPoint = new DoublePoint(
                (lineB.getPointA().getX() + lineB.getPointB().getX())/2,
                (lineB.getPointA().getY() + lineB.getPointB().getY())/2
        );
        DoublePoint lineBRect = new DoublePoint(
                Math.abs(lineB.getPointA().getX() - lineB.getPointB().getX()),
                Math.abs(lineB.getPointA().getY() - lineB.getPointB().getY())
        );

        if (    Math.abs(intersection.getX() - avgAPoint.getX()) <= lineARect.getX()/2 &&
                Math.abs(intersection.getY() - avgAPoint.getY()) <= lineARect.getY()/2 &&
                Math.abs(intersection.getX() - avgBPoint.getX()) <= lineBRect.getX()/2 &&
                Math.abs(intersection.getY() - avgBPoint.getY()) <= lineBRect.getY()/2
        )
            return Collections.singleton(intersection);
        else
            return Collections.emptySet();
    }

    private static Set<DoublePoint> verticalLineIntersection(Line vertical, Line other) {
        double otherAngleRatio = (other.getPointB().getY() - other.getPointA().getY()) / (other.getPointB().getX() - other.getPointA().getX());
        DoublePoint intersection = new DoublePoint(vertical.getPointA().getX(), 0);
        double line2Offset = other.getPointA().getY() - otherAngleRatio * other.getPointA().getX();
        intersection.setY(otherAngleRatio * intersection.getX() + line2Offset);

        if (    (other.getPointA().getX() < intersection.getX() && other.getPointB().getX() < intersection.getX()) ||
                (other.getPointA().getX() > intersection.getX() && other.getPointB().getX() > intersection.getX())
        ) return Collections.emptySet();

        double avgY = (vertical.getPointA().getY() + vertical.getPointB().getY())/2;
        double height = Math.abs(vertical.getPointA().getY() - vertical.getPointB().getY());
        if (Math.abs(intersection.getY() - avgY) <= height/2)
            return Collections.singleton(intersection);
        else
            return Collections.emptySet();
    }
}
