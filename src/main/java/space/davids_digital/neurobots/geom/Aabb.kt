package space.davids_digital.neurobots.geom

data class Aabb (val min: DoublePoint = DoublePoint(), val max: DoublePoint = DoublePoint()) {
    constructor(minX: Number, minY: Number, maxX: Number, maxY: Number):
            this(DoublePoint(minX.toDouble(), minY.toDouble()), DoublePoint(maxX.toDouble(), maxY.toDouble()))

    val center: DoublePoint get() = DoublePoint((min.x + max.x)/2, (min.y + max.y)/2)
    val width: Double get() = max.x - min.x
    val height: Double get() = max.y - min.y
}