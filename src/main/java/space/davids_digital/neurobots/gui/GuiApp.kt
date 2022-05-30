package space.davids_digital.neurobots.gui

import space.davids_digital.neurobots.geom.DoublePoint
import space.davids_digital.neurobots.model.*
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.Timer
import javax.swing.UIManager
import javax.swing.WindowConstants

class GuiApp private constructor() {
    private lateinit var worldViewer: WorldViewer
    private lateinit var world: World
    private lateinit var frame: JFrame

    init {
        initWorld()
        initGui()
        initUpdating()
        initRendering()
    }

    private fun initGui() {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        frame = JFrame("Neurobots")
        frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        frame.size = Dimension(1200, 800)
        worldViewer = WorldViewer(world)
        frame.layout = BorderLayout()
        frame.add(worldViewer, BorderLayout.CENTER)
        frame.add(ControlPanel(world), BorderLayout.EAST)
        worldViewer.grabFocus()
        frame.isVisible = true
    }

    private fun initWorld() {
        world = World(1200, 800)
        world.walls.add(Wall(DoublePoint(), DoublePoint(0, world.height)))
        world.walls.add(Wall(DoublePoint(0, world.height), DoublePoint(world.width, world.height)))
        world.walls.add(Wall(DoublePoint(world.width, world.height), DoublePoint(world.width, 0)))
        world.walls.add(Wall(DoublePoint(world.width, 0), DoublePoint()))
        world.spawners.add(CreatureSpawner(
            world,
            DoublePoint(150, 150),
            Color(0f, 0.5f, 0f),
            1,
            6,
            250.0,
            8,
            100.0, 100.0,
            100.0, 100.0,
            50.0,
            Math.toRadians(75.0)
        ))
        world.spawners.add(CreatureSpawner(
            world,
            DoublePoint(300, 150),
            Color(0f, 0.5f, 0f),
            1,
            6,
            250.0,
            8,
            100.0, 100.0,
            100.0, 100.0,
            50.0,
            Math.toRadians(75.0)
        ))
    }

    private fun initUpdating() {
        var lastUpdate = System.currentTimeMillis().toDouble()
        val timer = Timer(10) {
            val delta: Double = System.currentTimeMillis() - lastUpdate
            lastUpdate = System.currentTimeMillis().toDouble()
            world.update(delta)
            worldViewer.update(delta)
        }
        timer.isRepeats = true
        timer.start()
    }

    private fun initRendering() {
        val timer = Timer(10) { frame.repaint() }
        timer.isRepeats = true
        timer.start()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            GuiApp()
        }
    }
}