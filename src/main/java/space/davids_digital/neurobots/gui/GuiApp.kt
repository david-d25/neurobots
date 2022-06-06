package space.davids_digital.neurobots.gui

import space.davids_digital.neurobots.game.Config
import space.davids_digital.neurobots.geom.DoublePoint
import space.davids_digital.neurobots.world.*
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.lang.Math.random
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
    private var config = Config()

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
        worldViewer = WorldViewer(world, config)
        frame.layout = BorderLayout()
        frame.add(worldViewer, BorderLayout.CENTER)
        frame.add(ControlPanel(world), BorderLayout.EAST)
        worldViewer.grabFocus()
        frame.isVisible = true
    }

    private fun initWorld() {
        world = World(2000, 2000, 0.005, 0.2, 1.0)
        val w = world.width - 1
        val h = world.height - 1
        world += Wall(0, 0, 0, h)
        world += Wall(0, h, w, h)
        world += Wall(w, h, w, 0)
        world += Wall(w, 0, 0, 0)
        world += Wall(w/2, h/2 - h/5, w/2, h/2 + h/5)
        world += Wall(w/2 - w/5, h/2, w/2 + w/5, h/2)
        world += FoodSpawner(DoublePoint(0, 0), DoublePoint(world.width, world.height), 20.0, 10.0, 100)
        repeat(10) {
            world += CreatureSpawner(
                DoublePoint(random()*world.width, random()*world.height),
                Color(random().toFloat(), random().toFloat(), random().toFloat()),
                2,
                8,
                1000.0,
                7,
                50.0, 200.0,
                50.0, 100.0,
                50.0,
                Math.toRadians(75.0),
                4
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