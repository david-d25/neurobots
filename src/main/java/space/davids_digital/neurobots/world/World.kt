package space.davids_digital.neurobots.world

import space.davids_digital.neurobots.geom.*
import java.util.LinkedList
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sqrt

class World(
    var width: Int,
    var height: Int,
    val movingEnergyCost: Double = 0.0,
    val rotationEnergyCost: Double = 0.0,
    val healingRate: Double = 0.0
) {
    val walls: MutableList<Wall> = mutableListOf()
    val creatures: MutableList<Creature> = CopyOnWriteArrayList()
    val creatureSpawners: MutableList<CreatureSpawner> = mutableListOf()
    val food: ConcurrentLinkedQueue<Food> = ConcurrentLinkedQueue()

    private val objects: ConcurrentLinkedQueue<WorldObject> = ConcurrentLinkedQueue()
    private val updateList: ConcurrentLinkedQueue<Updatable> = ConcurrentLinkedQueue()
    private val physicalBodies: ConcurrentLinkedQueue<PhysicalBody> = ConcurrentLinkedQueue()

    val kdTree = KdTree(KdTree.Node(KdTree.Axis.X, Aabb(0, 0, width, height), physicalBodies))

    var paused = false

    fun update(delta: Double) {
        if (paused) return
        if (creatures.size == 0) {
            creatureSpawners.forEach(CreatureSpawner::spawn)
            rebuildKdTree()
        }

        updateList.forEach { it.update(delta) }
        creatures.filter { !it.alive }.forEach { remove(it) }

        rebuildKdTree()

        creatures.forEach { creature ->
            val intersectionCandidates = getIntersectionCandidates(creature)

            intersectionCandidates.forEach {
                if (it is Creature) {
                    val distance = creature.position.distance(it.position)
                    val sin = (it.position.y - creature.position.y) / distance
                    val cos = (it.position.x - creature.position.x) / distance
                    if (sin.isFinite() && cos.isFinite() && distance < creature.radius + it.radius) {
                        val offset = creature.radius + it.radius - distance
                        creature.position.x -= offset * cos
                        creature.position.y -= offset * sin
                        it.position.x += offset * cos
                        it.position.y += offset * sin
                    }
                } else if (it is Wall) {
                    val pointADistance = creature.position.distance(it.pointA)
                    val pointBDistance = creature.position.distance(it.pointB)
                    val nearestEdgeDistance = min(pointADistance, pointBDistance)
                    val intersection = GeometryUtils.getLinePerpendicularIntersectionPoint(
                        Line(it.pointA, it.pointB), creature.position
                    )
                    var perpendicularLength: Double? = null
                    if (intersection != null)
                        perpendicularLength = creature.position.distance(intersection)
                    if (nearestEdgeDistance < creature.radius ||
                        perpendicularLength != null && perpendicularLength < creature.radius
                    ) {
                        val offset = if (perpendicularLength != null && perpendicularLength < nearestEdgeDistance) {
                            creature.position - intersection!!
                        } else {
                            val nearestPoint = if (pointADistance < pointBDistance) it.pointA else it.pointB
                            creature.position - nearestPoint
                        }
                        val sin = offset.y / sqrt(offset.x.pow(2) + offset.y.pow(2))
                        val cos = sqrt(1 - sin*sin)
                        creature.position.x += creature.radius * sign(offset.x) * cos - offset.x
                        creature.position.y += creature.radius * sin - offset.y
                    }
                } else if (it is Food) {
                    val distance = creature.position.distance(it.position)
                    val sin = (it.position.y - creature.position.y) / distance
                    val cos = (it.position.x - creature.position.x) / distance
                    if (sin.isFinite() && cos.isFinite() && distance < creature.radius + it.radius) {
                        this -= it
                        creature.changeEnergy(it.energy)
                    }
                }
            }

            creature.position.x = creature.position.x.coerceIn(creature.radius, width - creature.radius)
            creature.position.y = creature.position.y.coerceIn(creature.radius, height - creature.radius)
        }

        rebuildKdTree()
        creatures.forEach(Creature::updateRayData)
    }

    private fun getIntersectionCandidates(physicalBody: PhysicalBody): Set<PhysicalBody> {
        val candidates = mutableSetOf<PhysicalBody>()
        val nodesStack = LinkedList<KdTree.Node>()
        nodesStack.push(kdTree.root)
        while (nodesStack.isNotEmpty()) {
            val node = nodesStack.pop()

            if (node.objects.contains(physicalBody)) {
                if (node.final) {
                    candidates += node.objects
                } else {
                    nodesStack.push(node.left)
                    nodesStack.push(node.right)
                }
            }
        }
        candidates -= physicalBody
        return candidates
    }

    fun raycast(rayLine: Line): Set<RaycastResult> {
        val candidates = mutableSetOf<PhysicalBody>()

        val nodesStack = LinkedList<KdTree.Node>()
        nodesStack.push(kdTree.root)

        while (nodesStack.isNotEmpty()) {
            val node = nodesStack.pop()

            if (GeometryUtils.isInside(rayLine.pointA, node.aabb) ||
                GeometryUtils.isInside(rayLine.pointB, node.aabb) ||
                GeometryUtils.areSurfacesIntersecting(rayLine, node.aabb)
            ) {
                if (!node.final) {
                    nodesStack.push(node.left)
                    nodesStack.push(node.right)
                } else {
                    candidates += node.objects
                }
            }
        }

        val result = mutableSetOf<RaycastResult>()
        candidates.forEach {
            if (it is Wall) {
                val hits = GeometryUtils.surfaceIntersections(rayLine, Line(it.pointA, it.pointB))
                if (hits.isNotEmpty())
                    result += RaycastResult(it, hits)
            } else if (it is Creature) {
                val hits = GeometryUtils.surfaceIntersections(rayLine, Circle(it.position, it.radius))
                if (hits.isNotEmpty())
                    result += RaycastResult(it, hits)
            } else if (it is Food) {
                val hits = GeometryUtils.surfaceIntersections(rayLine, Circle(it.position, it.radius))
                if (hits.isNotEmpty())
                    result += RaycastResult(it, hits)
            }
            // TODO
        }
        return result
    }

    private fun rebuildKdTree() {
        kdTree.root.left = null
        kdTree.root.right = null
        kdTree.root.final = true

        val nodesStack = LinkedList<KdTree.Node>()
        nodesStack.push(kdTree.root)

        while (nodesStack.isNotEmpty()) {
            val node = nodesStack.pop()

            val sorted = node.objects.sortedBy {
                if (node.axis == KdTree.Axis.X)
                    it.aabb.center.x
                else
                    it.aabb.center.y
            }

            if (sorted.size < 2)
                continue

            val midPoint = (sorted[sorted.size/2].aabb.center + (sorted[sorted.size/2 - 1].aabb.center))/2

            val splitLine: Double
            val leftAabb: Aabb
            val rightAabb: Aabb

            if (node.axis == KdTree.Axis.X) {
                splitLine = midPoint.x
                leftAabb = Aabb(node.aabb.min.x, node.aabb.min.y, splitLine, node.aabb.max.y)
                rightAabb = Aabb(splitLine, node.aabb.min.y, node.aabb.max.x, node.aabb.max.y)
            } else {
                splitLine = midPoint.y
                leftAabb = Aabb(node.aabb.min.x, node.aabb.min.y, node.aabb.max.x, splitLine)
                rightAabb = Aabb(node.aabb.min.x, splitLine, node.aabb.max.x, node.aabb.max.y)
            }

            val leftList = mutableListOf<PhysicalBody>()
            val rightList = mutableListOf<PhysicalBody>()

            sorted.forEach {
                if (GeometryUtils.areVolumesIntersecting(it.aabb, leftAabb))
                    leftList.add(it)
                if (GeometryUtils.areVolumesIntersecting(it.aabb, rightAabb))
                    rightList.add(it)
            }

            if (leftList.size > 0 && rightList.size > 0 && leftList.size < sorted.size && rightList.size < sorted.size) {
                node.final = false
                val newAxis = if (node.axis == KdTree.Axis.X) KdTree.Axis.Y else KdTree.Axis.X
                node.left = KdTree.Node(newAxis, leftAabb, leftList)
                node.right = KdTree.Node(newAxis, rightAabb, rightList)
                node.splitLine = splitLine
                nodesStack.push(node.left)
                nodesStack.push(node.right)
            }
        }
    }

    private fun add(worldObject: WorldObject) {
        objects += worldObject
        if (worldObject is Wall)
            walls += worldObject
        if (worldObject is Creature)
            creatures += worldObject
        if (worldObject is CreatureSpawner)
            creatureSpawners += worldObject
        if (worldObject is PhysicalBody)
            physicalBodies += worldObject
        if (worldObject is Updatable)
            updateList += worldObject
        if (worldObject is Food)
            food += worldObject

        if (worldObject is WorldAware)
            worldObject.world = this
    }

    private fun remove(worldObject: WorldObject) {
        objects -= worldObject
        if (worldObject is Wall)
            walls -= worldObject
        if (worldObject is Creature)
            creatures -= worldObject
        if (worldObject is CreatureSpawner)
            creatureSpawners -= worldObject
        if (worldObject is PhysicalBody)
            physicalBodies -= worldObject
        if (worldObject is Updatable)
            updateList -= worldObject
        if (worldObject is Food)
            food -= worldObject

        if (worldObject is WorldAware)
            worldObject.world = NULL
    }

    operator fun plusAssign(worldObject: WorldObject) {
        add(worldObject)
    }

    operator fun minusAssign(worldObject: WorldObject) {
        remove(worldObject)
    }

    companion object {
        val NULL = World(0, 0, 0.0, 0.0, 0.0)
    }

    data class RaycastResult(val target: PhysicalBody, val hits: Set<DoublePoint>)
}