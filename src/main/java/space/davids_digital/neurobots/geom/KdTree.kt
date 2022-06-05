package space.davids_digital.neurobots.geom

import space.davids_digital.neurobots.world.PhysicalBody

class KdTree(val root: Node) {
    class Node(var axis: Axis, var aabb: Aabb, var objects: Collection<PhysicalBody> = emptySet()) {
        var left: Node? = null
        var right: Node? = null
        var final = true
        var splitLine = 0.0
    }

    enum class Axis { X, Y }
}