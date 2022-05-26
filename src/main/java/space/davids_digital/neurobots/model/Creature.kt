package space.davids_digital.neurobots.model

import space.davids_digital.neurobots.geom.DoublePoint
import space.davids_digital.neurobots.geom.GeometryUtils
import space.davids_digital.neurobots.geom.Line
import java.awt.Color
import java.util.*
import java.util.function.Consumer
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class Creature(
    var neuralNetwork: NeuralNetwork,
    var color: Color,
    var position: DoublePoint,
    var visionDistance: Double,
    var raysNumber: Int,
    var angle: Double,
    var fitness: Double,
    var energy: Double,
    val maxEnergy: Double,
    var radius: Double,
    var fov: Double
) {
    var isAlive = true

    @Transient
    val wallRayData: DoubleArray = DoubleArray(raysNumber)

    fun update(world: World, delta: Double) {
        val input = DoubleArray(raysNumber + 1)
        System.arraycopy(wallRayData, 0, input, 0, wallRayData.size)
        input[wallRayData.size] = energy / maxEnergy // energy
        val output = neuralNetwork.getResponse(input)
        val forward = output[0] * delta
        val right = output[1] * delta
        val rotation = output[2] * delta / 100
        angle += rotation
        position.x =
            position.x + cos(angle) * forward + cos(angle + Math.PI / 2) * right
        position.y =
            position.y + sin(angle) * forward + sin(angle + Math.PI / 2) * right
        updateRayData(world)
    }

    private fun updateRayData(world: World) {
        val x = position.x
        val y = position.y
        Arrays.fill(wallRayData, 0.0)
        world.walls.forEach(Consumer { wall: Wall ->
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
                wallRayData[i] = wallRayData[i].coerceAtLeast(1 - distance / maxDistance)
            }
        })
    }
}