package space.davids_digital.neurobots.model

import space.davids_digital.neurobots.geom.DoublePoint

class Bullet(var firedBy: Creature, var damage: Double, var position: DoublePoint, var speed: DoublePoint)