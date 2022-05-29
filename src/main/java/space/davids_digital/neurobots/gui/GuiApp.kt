package space.davids_digital.neurobots.gui

import space.davids_digital.neurobots.geom.DoublePoint
import space.davids_digital.neurobots.model.Creature
import space.davids_digital.neurobots.model.NeuralNetwork
import space.davids_digital.neurobots.model.Wall
import space.davids_digital.neurobots.model.World
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
        world.walls.add(Wall(DoublePoint(), DoublePoint(0.0, world.height.toDouble())))
        world.walls.add(
            Wall(
                DoublePoint(0.0, world.height.toDouble()),
                DoublePoint(world.width.toDouble(), world.height.toDouble())
            )
        )
        world.walls.add(
            Wall(
                DoublePoint(world.width.toDouble(), world.height.toDouble()),
                DoublePoint(world.width.toDouble(), 0.0)
            )
        )
        world.walls.add(Wall(DoublePoint(world.width.toDouble(), 0.0), DoublePoint()))
        for (i in 0..29) world.creatures.add(
            Creature(
                NeuralNetwork(8*2+2, 2, 11, 3, -0.25, 0.25),
                Color.green,
                DoublePoint((100 + 10 * i).toDouble(), 40.0),
                100.0, 8, Math.random()*Math.PI, 100.0, 100.0, 100.0, 100.0, 30.0, Math.toRadians(75.0)
            )
        )
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