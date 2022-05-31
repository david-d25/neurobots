package space.davids_digital.neurobots.gui

import space.davids_digital.neurobots.geom.DoublePoint
import space.davids_digital.neurobots.world.*
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.Timer
import javax.swing.UIManager
import javax.swing.WindowConstants
import kotlin.math.min
import kotlin.math.sin

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
        world = World(10000, 5000, 0.04, 0.04, 1.0)
        world += Wall(DoublePoint(), DoublePoint(0, world.height))
        world += Wall(DoublePoint(0, world.height), DoublePoint(world.width, world.height))
        world += Wall(DoublePoint(world.width, world.height), DoublePoint(world.width, 0))
        world += Wall(DoublePoint(world.width, 0), DoublePoint())
        world += FoodSpawner(DoublePoint(0, 0), DoublePoint(world.width, world.height), 10.0, 15.0, 10000)
        repeat(32) {
            world += CreatureSpawner(
                DoublePoint(150 + 250.0 * it, 2500 + 1500 * sin(200.0*it)),
                Color(0f, 0.5f, 0f),
                1,
                6,
                1000.0,
                8,
                50.0, 200.0,
                50.0, 100.0,
                50.0,
                Math.toRadians(75.0)
            )
        }
    }

    private fun initUpdating() {
        var lastUpdate = System.currentTimeMillis().toDouble()
        val timer = Timer(10) {
            val delta = min(System.currentTimeMillis() - lastUpdate, 30.0)
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