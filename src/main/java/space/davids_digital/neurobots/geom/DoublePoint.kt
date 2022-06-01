package space.davids_digital.neurobots.geom

import kotlin.math.pow
import kotlin.math.sqrt

data class DoublePoint(var x: Double = 0.0, var y: Double = 0.0) {

    constructor(x: Int, y: Int): this(x.toDouble(), y.toDouble())

    fun distance(other: DoublePoint) = sqrt((x - other.x).pow(2) + (y - other.y).pow(2))

    fun copy() = DoublePoint(x, y)

    operator fun plus(other: DoublePoint) = DoublePoint(x + other.x, y + other.y)
    operator fun minus(other: DoublePoint) = DoublePoint(x - other.x, y - other.y)
}