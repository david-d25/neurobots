package space.davids_digital.neurobots.geom

class Line (var pointA: DoublePoint, var pointB: DoublePoint){
    val middle get() = DoublePoint((pointA.x + pointB.x)/2, (pointA.y + pointB.y) / 2)
    val length get() = pointA.distance(pointB)
}