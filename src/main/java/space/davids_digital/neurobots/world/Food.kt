package space.davids_digital.neurobots.world

import space.davids_digital.neurobots.geom.Aabb
import space.davids_digital.neurobots.geom.DoublePoint

class Food(val position: DoublePoint, val radius: Double, val energy: Double): WorldObject(), PhysicalBody {
    override val aabb: Aabb
        get() = Aabb(position.x - radius, position.y - radius, position.x + radius, position.y + radius)
}