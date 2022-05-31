package space.davids_digital.neurobots.world

import space.davids_digital.neurobots.geom.DoublePoint
import java.awt.Color
import java.lang.Math.PI
import java.lang.Math.random

class CreatureSpawner (
    val position: DoublePoint,
    val color: Color,
    val hiddenLayers: Int,
    val hiddenLayerSize: Int,
    var visionDistance: Double,
    var raysNumber: Int,
    var energy: Double,
    val maxEnergy: Double,
    var health: Double,
    var maxHealth: Double,
    var radius: Double,
    var fov: Double,
): WorldObject() {
    fun spawn() {
        world += Creature(
            NeuralNetwork(
                raysNumber*3 + 3,
                hiddenLayers,
                hiddenLayerSize,
                5,
                -1.0,
                1.0
            ),
            color,
            position.copy(),
            visionDistance,
            raysNumber,
            2*PI*random(),
            energy,
            maxEnergy,
            health,
            maxHealth,
            radius,
            fov
        )
    }
}