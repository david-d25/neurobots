package space.davids_digital.neurobots.model

import space.davids_digital.neurobots.geom.GeometryUtils
import space.davids_digital.neurobots.geom.Line
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sqrt

class World(
    var width: Int,
    var height: Int,
    val movingEnergyCost: Double = 0.0,
    val rotationEnergyCost: Double = 0.0,
) {
    val walls: MutableList<Wall> = mutableListOf()
    val creatures: MutableList<Creature> = mutableListOf()
    val bullets: MutableList<Bullet> = mutableListOf()
    val spawners: MutableList<CreatureSpawner> = mutableListOf()
    val food: MutableList<Food> = mutableListOf()
    var paused = false

    fun update(delta: Double) {
        if (paused) return
        if (creatures.size == 0)
            spawners.forEach(CreatureSpawner::spawn)

        creatures.forEach { it.update(this, delta) }

        creatures.filter(Creature::alive).forEach { c1 ->
            creatures.filter(Creature::alive).filter { it !== c1 }.forEach { c2 ->
                val distance = c1.position.distance(c2.position)
                val sin = (c2.position.y - c1.position.y) / distance
                val cos = (c2.position.x - c1.position.x) / distance
                if (sin.isFinite() && cos.isFinite() && distance < c1.radius + c2.radius) {
                    val offset = c1.radius + c2.radius - distance
                    c1.position.x -= offset * cos
                    c1.position.y -= offset * sin
                    c2.position.x += offset * cos
                    c2.position.y += offset * sin
                }
            }
            if (c1.position.x < c1.radius)
                c1.position.x = c1.radius
            if (c1.position.y < c1.radius)
                c1.position.y = c1.radius
            if (c1.position.x > width - c1.radius)
                c1.position.x = width.toDouble() - c1.radius
            if (c1.position.y > height - c1.radius)
                c1.position.y = height.toDouble() - c1.radius
        }

        creatures.filter(Creature::alive).forEach { c ->
            val iterator = food.iterator()
            while (iterator.hasNext()) {
                val f = iterator.next()
                val distance = c.position.distance(f.position)
                val sin = (f.position.y - c.position.y) / distance
                val cos = (f.position.x - c.position.x) / distance
                if (sin.isFinite() && cos.isFinite() && distance < c.radius + f.radius) {
                    iterator.remove()
                    c.changeEnergy(f.energy)
                }
            }
        }

        creatures.filter(Creature::alive).forEach { creature ->
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
        }

        creatures.forEach { c: Creature -> c.updateRayData(this) }
    }
}