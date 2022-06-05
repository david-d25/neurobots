package space.davids_digital.neurobots.world

import space.davids_digital.neurobots.geom.Aabb

interface PhysicalBody {
    val aabb: Aabb
}