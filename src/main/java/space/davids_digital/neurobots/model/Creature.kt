package space.davids_digital.neurobots.model

import space.davids_digital.neurobots.geom.Circle
import space.davids_digital.neurobots.geom.DoublePoint
import space.davids_digital.neurobots.geom.GeometryUtils
import space.davids_digital.neurobots.geom.Line
import java.awt.Color
import java.util.*
import java.util.function.Consumer
import kotlin.math.*

class Creature(
    var neuralNetwork: NeuralNetwork,
    var color: Color,
    var position: DoublePoint,
    var visionDistance: Double,
    var raysNumber: Int,
    var angle: Double,
    var energy: Double,
    val maxEnergy: Double,
    var health: Double,
    var maxHealth: Double,
    var radius: Double,
    var fov: Double
) {
    var isAlive = true

    @Transient
    val wallRayData: DoubleArray = DoubleArray(raysNumber)
    val creatureRayData: DoubleArray = DoubleArray(raysNumber)
    val foodRayData: DoubleArray = DoubleArray(raysNumber)

    fun update(world: World, delta: Double) {
        val input = DoubleArray(raysNumber*3 + 2)
        System.arraycopy(wallRayData, 0, input, 0, raysNumber)
        System.arraycopy(creatureRayData, 0, input, raysNumber, raysNumber)
        System.arraycopy(foodRayData, 0, input, raysNumber*2, raysNumber)
        input[wallRayData.size] = energy / maxEnergy
        input[wallRayData.size+1] = health / maxHealth
        val output = neuralNetwork.getResponse(input)
        val forward = output[0] * delta
        val right = output[1] * delta
        val rotation = output[2] * delta / 100
        angle += rotation
        position.x = position.x + cos(angle) * forward + cos(angle + Math.PI / 2) * right
        position.y = position.y + sin(angle) * forward + sin(angle + Math.PI / 2) * right
    }

    fun updateRayData(world: World) {
        val x = position.x
        val y = position.y
        Arrays.fill(creatureRayData, 0.0)
        Arrays.fill(wallRayData, 0.0)

        world.creatures.filter { it.isAlive && it !== this }.forEach {
            for (i in creatureRayData.indices) {
                val rayAngle = angle - fov / 2 + fov / raysNumber * (i + 0.5)
                val rayLine = Line(
                    DoublePoint(x + cos(rayAngle) * (radius - 0.1), y + sin(rayAngle) * (radius - 0.1)),
                    DoublePoint(
                        x + cos(rayAngle) * (radius + visionDistance),
                        y + sin(rayAngle) * (radius + visionDistance)
                    )
                )
                val intersectionPoints = GeometryUtils.intersections(rayLine, Circle(it.position, it.radius))
                if (intersectionPoints.size == 1) {
                    val collision = intersectionPoints.first()
                    val distance = rayLine.pointA.distance(collision)
                    creatureRayData[i] = max(creatureRayData[i], 1 - distance/rayLine.length)
                } else if (intersectionPoints.size == 2) {
                    val distance = min(rayLine.pointA.distance(intersectionPoints.first()), rayLine.pointA.distance(intersectionPoints.last()))
                    creatureRayData[i] = max(creatureRayData[i], 1 - distance/rayLine.length)
                }
            }
        }

        world.walls.forEach { wall: Wall ->
            for (i in wallRayData.indices) {
                val rayAngle = angle - fov / 2 + fov / raysNumber * (i + 0.5)
                val rayLine = Line(
                    DoublePoint(x + cos(rayAngle) * radius, y + sin(rayAngle) * radius),
                    DoublePoint(
                        x + cos(rayAngle) * (radius + visionDistance),
                        y + sin(rayAngle) * (radius + visionDistance)
                    )
                )
                val wallLine = Line(wall.pointA, wall.pointB)
                val intersections = GeometryUtils.intersections(wallLine, rayLine)
                if (intersections.isEmpty()) continue
                val intersection = intersections.stream().findFirst().get()
                val distance = sqrt(
                    (rayLine.pointA.x - intersection.x).pow(2.0) + (rayLine.pointA.y - intersection.y).pow(2.0)
                )
                val maxDistance = sqrt(
                    (rayLine.pointA.x - rayLine.pointB.x).pow(2.0) + (rayLine.pointA.y - rayLine.pointB.y).pow(2.0)
                )
                wallRayData[i] = max(wallRayData[i], 1 - distance / maxDistance)
            }
        }

        for (i in 0 until raysNumber) {
            if (wallRayData[i] != 0.0 && creatureRayData[i] != 0.0) {
                if (wallRayData[i] > creatureRayData[i])
                    creatureRayData[i] = 0.0
                else
                    wallRayData[i] = 0.0
            }
        }
    }
}