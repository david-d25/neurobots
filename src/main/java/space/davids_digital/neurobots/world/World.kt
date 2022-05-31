package space.davids_digital.neurobots.world

import space.davids_digital.neurobots.geom.GeometryUtils
import space.davids_digital.neurobots.geom.Line
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
    val bullets: MutableList<Bullet> = mutableListOf()
    val creatureSpawners: MutableList<CreatureSpawner> = mutableListOf()
    val foodSpawners: MutableList<FoodSpawner> = mutableListOf()
    val food: MutableList<Food> = mutableListOf()
    val objects: ConcurrentLinkedQueue<WorldObject> = ConcurrentLinkedQueue()
    var paused = false

    fun update(delta: Double) {
        if (paused) return
        if (creatures.size == 0)
            creatureSpawners.forEach(CreatureSpawner::spawn)

        objects.forEach { it.update(delta) }

        creatures.filter { !it.alive }.forEach { remove(it) }

        creatures.forEach { creature ->
            creatures.filter { it !== creature }.forEach { innerCreature ->
                val distance = creature.position.distance(innerCreature.position)
                val sin = (innerCreature.position.y - creature.position.y) / distance
                val cos = (innerCreature.position.x - creature.position.x) / distance
                if (sin.isFinite() && cos.isFinite() && distance < creature.radius + innerCreature.radius) {
                    val offset = creature.radius + innerCreature.radius - distance
                    creature.position.x -= offset * cos
                    creature.position.y -= offset * sin
                    innerCreature.position.x += offset * cos
                    innerCreature.position.y += offset * sin
                }
            }
            if (creature.position.x < creature.radius)
                creature.position.x = creature.radius
            if (creature.position.y < creature.radius)
                creature.position.y = creature.radius
            if (creature.position.x > width - creature.radius)
                creature.position.x = width.toDouble() - creature.radius
            if (creature.position.y > height - creature.radius)
                creature.position.y = height.toDouble() - creature.radius

            walls.forEach { wall ->
                val pointADistance = creature.position.distance(wall.pointA)
                val pointBDistance = creature.position.distance(wall.pointB)
                val nearestEdgeDistance = min(pointADistance, pointBDistance)
                val intersection = GeometryUtils.getLinePerpendicularIntersectionPoint(
                    Line(wall.pointA, wall.pointB), creature.position
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
                        val nearestPoint = if (pointADistance < pointBDistance) wall.pointA else wall.pointB
                        creature.position - nearestPoint
                    }
                    val sin = offset.y / sqrt(offset.x.pow(2) + offset.y.pow(2))
                    val cos = sqrt(1 - sin*sin)
                    creature.position.x += creature.radius * sign(offset.x) * cos - offset.x
                    creature.position.y += creature.radius * sin - offset.y
                }
            }

            val iterator = food.iterator()
            while (iterator.hasNext()) {
                val f = iterator.next()
                val distance = creature.position.distance(f.position)
                val sin = (f.position.y - creature.position.y) / distance
                val cos = (f.position.x - creature.position.x) / distance
                if (sin.isFinite() && cos.isFinite() && distance < creature.radius + f.radius) {
                    iterator.remove()
                    creature.changeEnergy(f.energy)
                }
            }
        }

        creatures.forEach { it.updateRayData(this) }
    }

    operator fun plusAssign(worldObject: WorldObject) {
        add(worldObject)
    }

    fun add(worldObject: WorldObject) {
        worldObject.world = this
        objects += worldObject
        if (worldObject is Wall)
            walls += worldObject
        if (worldObject is Creature)
            creatures += worldObject
        if (worldObject is Bullet)
            bullets += worldObject
        if (worldObject is CreatureSpawner)
            creatureSpawners += worldObject
        if (worldObject is FoodSpawner)
            foodSpawners += worldObject
        if (worldObject is Food)
            food += worldObject
    }

    fun remove(worldObject: WorldObject) {
        objects -= worldObject
        if (worldObject is Wall)
            walls -= worldObject
        if (worldObject is Creature)
            creatures -= worldObject
        if (worldObject is Bullet)
            bullets -= worldObject
        if (worldObject is CreatureSpawner)
            creatureSpawners -= worldObject
        if (worldObject is FoodSpawner)
            foodSpawners -= worldObject
        if (worldObject is Food)
            food -= worldObject
    }

    companion object {
        val NULL = World(0, 0, 0.0, 0.0, 0.0)
    }
}