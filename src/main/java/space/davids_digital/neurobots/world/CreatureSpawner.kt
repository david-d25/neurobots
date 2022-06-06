package space.davids_digital.neurobots.world

import space.davids_digital.neurobots.geom.Aabb
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
    var memorySize: Int,
): WorldObject(), WorldAware {
    override var world: World = World.NULL
    fun spawn() {
        world += Creature(
            NeuralNetwork(
                raysNumber*3 + memorySize + 3,
                hiddenLayers,
                hiddenLayerSize,
                memorySize + 5,
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
            fov,
            memorySize
        )
    }
}