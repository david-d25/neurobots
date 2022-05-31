package space.davids_digital.neurobots.model

import space.davids_digital.neurobots.geom.DoublePoint
import java.awt.Color
import java.lang.Math.PI
import java.lang.Math.random

class CreatureSpawner (
    val world: World,
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
) {
    fun spawn() {
        world.creatures.add(
            Creature(
                NeuralNetwork(
                    raysNumber*3 + 3,
                    hiddenLayers,
                    hiddenLayerSize,
                    5,
                    -0.5,
                    0.5
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
        )
    }
}