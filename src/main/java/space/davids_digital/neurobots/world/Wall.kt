package space.davids_digital.neurobots.world

import space.davids_digital.neurobots.geom.Aabb
import space.davids_digital.neurobots.geom.DoublePoint

class Wall(var pointA: DoublePoint, var pointB: DoublePoint): WorldObject(), RigidBody {
    constructor(xA: Number, yA: Number, xB: Number, yB: Number): this(DoublePoint(xA, yA), DoublePoint(xB, yB))

    override val aabb: Aabb
        get() = Aabb(pointA, pointB)
}