package space.davids_digital.neurobots.world

import space.davids_digital.neurobots.geom.*
import java.awt.Color
import java.lang.Math.random
import java.util.*
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
    var fov: Double,
): WorldObject(), WorldAware, PhysicalBody, Updatable {
    override var world: World = World.NULL

    var alive = true
    var lifetime = 0.0

    @Transient
    val wallRayData: DoubleArray = DoubleArray(raysNumber)
    val creatureRayData: DoubleArray = DoubleArray(raysNumber)
    val foodRayData: DoubleArray = DoubleArray(raysNumber)

    override fun update(delta: Double) {
        if (!alive) return
        val input = DoubleArray(neuralNetwork.inputsN)
        System.arraycopy(wallRayData, 0, input, 0, raysNumber)
        System.arraycopy(creatureRayData, 0, input, raysNumber, raysNumber)
        System.arraycopy(foodRayData, 0, input, raysNumber*2, raysNumber)
        input[raysNumber*2] = energy / maxEnergy
        input[raysNumber*2+1] = health / maxHealth
        input[raysNumber*2+2] = 1.0
        val output = neuralNetwork.getResponse(input)
        val forward = output[0] * delta
        val right = output[1] * delta
        val rotation = output[2] * Math.toRadians(delta)
        val deltaX = cos(angle) * forward + cos(angle + Math.PI / 2) * right
        val deltaY = sin(angle) * forward + sin(angle + Math.PI / 2) * right
        angle += rotation
        position.x += deltaX
        position.y += deltaY
        changeEnergy(
            - world.rotationEnergyCost * abs(rotation) - world.movingEnergyCost * sqrt(deltaX*deltaX + deltaY*deltaY)
        )
        if (energy > 0 && health < maxHealth) {
            val healing = min(delta/1000*world.healingRate, energy)
            changeEnergy(-healing)
            changeHealth(healing)
        }
        if (output[3] > 0.5)
            divide(world)

        lifetime += delta
        changeHealth(-exp(lifetime/10000)*delta/1000000000)
    }

    override val aabb: Aabb get() {
        return Aabb(
            DoublePoint(position.x - radius, position.y - radius),
            DoublePoint(position.x + radius, position.y + radius)
        )
    }

    fun changeEnergy(delta: Double) {
        energy += delta
        if (energy < 0) {
            changeHealth(energy)
            energy = 0.0
        }
        if (energy > maxEnergy)
            energy = maxEnergy
    }

    fun changeHealth(delta: Double) {
        health += delta
        if (health < 0)
            alive = false
        if (health > maxHealth)
            health = maxHealth
    }

    private fun divide(world: World) {
        val newCreature = Creature(
            neuralNetwork.copy().mutate(0.001),
            color,
            position.copy().also {
                it.x += 200 - random()*400
                it.y += 200 - random()*400
            },
            visionDistance,
            raysNumber,
            angle,
            energy/2,
            maxEnergy,
            health,
            maxHealth,
            radius,
            fov
        )
        newCreature.changeEnergy(-maxEnergy/12)
        changeEnergy(- energy/2 - maxEnergy/12)
        world += newCreature
    }

    fun updateRayData() {
        val x = position.x
        val y = position.y
        Arrays.fill(creatureRayData, 0.0)
        Arrays.fill(wallRayData, 0.0)
        Arrays.fill(foodRayData, 0.0)

        for (i in 0 until raysNumber) {
            val rayAngle = angle - fov / 2 + fov / raysNumber * (i + 0.5)
            val rayLine = Line(
                DoublePoint(x + cos(rayAngle) * (radius - 0.1), y + sin(rayAngle) * (radius - 0.1)),
                DoublePoint(
                    x + cos(rayAngle) * (radius + visionDistance),
                    y + sin(rayAngle) * (radius + visionDistance)
                )
            )

            val raycastResults = world.raycast(rayLine)

            raycastResults.forEach {
                val target = it.target
                val hits = it.hits

                if (target is Food || target is Creature && target.alive && target !== this) {
                    val rayDataRef = if (target is Food) foodRayData else creatureRayData
                    if (hits.size == 1) {
                        val collision = hits.first()
                        val distance = rayLine.pointA.distance(collision)
                        rayDataRef[i] = max(rayDataRef[i], 1 - distance/rayLine.length)
                    } else if (hits.size == 2) {
                        val distance = min(
                            rayLine.pointA.distance(hits.first()),
                            rayLine.pointA.distance(hits.last())
                        )
                        rayDataRef[i] = max(rayDataRef[i], 1 - distance/rayLine.length)
                    }
                } else if (target is Wall) {
                    val intersection = hits.first()
                    val distance = sqrt(
                        (rayLine.pointA.x - intersection.x).pow(2.0) + (rayLine.pointA.y - intersection.y).pow(2.0)
                    )
                    val maxDistance = sqrt(
                        (rayLine.pointA.x - rayLine.pointB.x).pow(2.0) + (rayLine.pointA.y - rayLine.pointB.y).pow(2.0)
                    )
                    wallRayData[i] = max(wallRayData[i], 1 - distance / maxDistance)
                }
            }
        }


        for (i in 0 until raysNumber) {
            if (wallRayData[i] > creatureRayData[i]) {
                creatureRayData[i] = 0.0
                if (wallRayData[i] > foodRayData[i])
                    foodRayData[i] = 0.0
                else
                    wallRayData[i] = 0.0
            } else {
                wallRayData[i] = 0.0
                if (creatureRayData[i] > foodRayData[i])
                    foodRayData[i] = 0.0
                else
                    creatureRayData[i] = 0.0
            }
        }
    }
}