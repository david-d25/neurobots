package space.davids_digital.neurobots.world

import space.davids_digital.neurobots.geom.DoublePoint

class FoodSpawner(
    val minCoords: DoublePoint,
    val maxCoords: DoublePoint,
    val energy: Double,
    val ratio: Double,
    val initialNumber: Int = 0
): WorldObject(), WorldAware, Updatable {
    private var timePassed = 0.0

    override var world: World = World.NULL
        set(value) {
            field = value
            repeat(initialNumber) {
                spawn()
            }
        }

    override fun update(delta: Double) {
        timePassed += delta
        while (timePassed > 1000/ratio) {
            spawn()
            timePassed -= 1000/ratio
        }
    }

    private fun spawn() {
        world += Food(
            DoublePoint(
                minCoords.x + Math.random()*(maxCoords.x - minCoords.x),
                minCoords.y + Math.random()*(maxCoords.y - minCoords.y)
            ),
            energy,
            energy
        )
    }
}