package space.davids_digital.neurobots.world

import space.davids_digital.neurobots.geom.Aabb
import space.davids_digital.neurobots.geom.DoublePoint
import kotlin.math.max
import kotlin.math.min

class Wall(var pointA: DoublePoint, var pointB: DoublePoint): WorldObject(), PhysicalBody {
    constructor(xA: Number, yA: Number, xB: Number, yB: Number): this(DoublePoint(xA, yA), DoublePoint(xB, yB))

    override val aabb: Aabb
        get() = Aabb(min(pointA.x, pointB.x), min(pointA.y, pointB.y), max(pointA.x, pointB.x), max(pointA.y, pointB.y))
}