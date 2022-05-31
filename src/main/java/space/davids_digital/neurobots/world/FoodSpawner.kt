package space.davids_digital.neurobots.world

import space.davids_digital.neurobots.geom.DoublePoint

class FoodSpawner(
    val world: World,
    val minCoords: DoublePoint,
    val maxCoords: DoublePoint,
    val energy: Double,
    val ratio: Double,
    initialNumber: Int = 0
) {
    private var timePassed = 0.0

    init {
        repeat(initialNumber) {
            spawn()
        }
    }

    fun update(delta: Double) {
        timePassed += delta
        while (timePassed > 1000/ratio) {
            spawn()
            timePassed -= 1000/ratio
        }
    }

    private fun spawn() {
        world.food.add(Food(
            DoublePoint(
                minCoords.x + Math.random()*(maxCoords.x - minCoords.x),
                minCoords.y + Math.random()*(maxCoords.y - minCoords.y)
            ),
            energy,
            energy
        ))
    }
}