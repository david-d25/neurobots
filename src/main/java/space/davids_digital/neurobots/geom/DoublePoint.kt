package space.davids_digital.neurobots.geom

import kotlin.math.pow
import kotlin.math.sqrt

class DoublePoint(var x: Double = 0.0, var y: Double = 0.0) {
    fun distance(other: DoublePoint) = sqrt((x - other.x).pow(2) + (y - other.y).pow(2))

    operator fun plus(other: DoublePoint) = DoublePoint(x + other.x, y + other.y)
    operator fun minus(other: DoublePoint) = DoublePoint(x - other.x, y - other.y)
}