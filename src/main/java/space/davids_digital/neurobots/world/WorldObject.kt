package space.davids_digital.neurobots.world

open class WorldObject: Updatable, WorldAware {
    override var world: World = World.NULL
    override fun update(delta: Double) {}
}