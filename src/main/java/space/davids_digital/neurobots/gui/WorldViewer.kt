package space.davids_digital.neurobots.gui

import space.davids_digital.neurobots.game.Config
import space.davids_digital.neurobots.geom.DoublePoint
import space.davids_digital.neurobots.geom.KdTree
import space.davids_digital.neurobots.world.Creature
import space.davids_digital.neurobots.world.PhysicalBody
import space.davids_digital.neurobots.world.World
import space.davids_digital.neurobots.world.WorldObject
import java.awt.*
import java.awt.event.*
import java.awt.geom.*
import java.util.LinkedList
import javax.swing.JPanel
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

class WorldViewer(
    var world: World,
    var config: Config,
): JPanel(), MouseListener, MouseMotionListener, MouseWheelListener, ComponentListener {
    private var cameraX = world.width/2.toDouble()
    private var cameraY = world.height/2.toDouble()
    private var viewportHeight = world.height.toDouble()
    private val cameraTransform = AffineTransform()
    private var lastMousePosition = Point()
    private var isDragging = false

    private var fpsCounterTicks = 0
    private var fpsCounterLastTime = System.nanoTime()
    private var fpsCounterLastFps = 0.0

    private var selection: WorldObject? = null

    init {
        addMouseMotionListener(this)
        addMouseWheelListener(this)
        addMouseListener(this)
        isFocusable = true
    }

    public override fun paintComponent(graphics: Graphics) {
        super.paintComponent(graphics)
        updateCameraTransform()
        val g = graphics as Graphics2D
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        g.color = Color.GRAY
        g.fillRect(0, 0, width, height)
        g.transform = cameraTransform
        g.color = Color.LIGHT_GRAY
        g.fillRect(0, 0, world.width, world.height)
        g.stroke = BasicStroke(2f)
        g.color = Color.BLACK
        world.walls.forEach { g.draw(Line2D.Double(it.pointA.x, it.pointA.y, it.pointB.x, it.pointB.y)) }

        world.creatureSpawners.forEach {
            val oldStroke = g.stroke
            g.stroke = BasicStroke(
                3f, BasicStroke.CAP_ROUND, BasicStroke.CAP_ROUND, 0f, arrayOf(4f, 4f).toFloatArray(), 0f
            )
            g.color = Color(0f, 0f, 0f, .4f)
            g.drawArc(
                (it.position.x - it.radius).toInt(),
                (it.position.y - it.radius).toInt(),
                it.radius.toInt() * 2,
                it.radius.toInt() * 2,
                0,
                360
            )
            g.color = Color(0f, 0f, 0f, .2f)
            g.fillArc(
                (it.position.x - it.radius).toInt(),
                (it.position.y - it.radius).toInt(),
                it.radius.toInt() * 2,
                it.radius.toInt() * 2,
                0,
                360
            )
            g.stroke = oldStroke
        }

        // Creature
        world.creatures.forEach { creature ->
            val x = creature.position.x
            val y = creature.position.y
            val radius = creature.radius
            val angle = creature.angle
            g.color = if (creature.alive) creature.color else Color(0f, 0f, 0f, .05f)
            g.fill(Arc2D.Double(
                x - radius,
                y - radius,
                creature.radius * 2,
                creature.radius * 2,
                0.0,
                360.0,
                Arc2D.CHORD
            ))

            if (!creature.alive)
                return@forEach

            g.color = creature.color.darker()
            g.draw(Arc2D.Double(
                x - radius,
                y - radius,
                creature.radius * 2,
                creature.radius * 2,
                0.0,
                360.0,
                Arc2D.CHORD
            ))

            val path = Path2D.Double()
            path.moveTo(x + 0.5 * radius * cos(angle + 0.8), y + 0.5 * radius * sin(angle + 0.8))
            path.lineTo(x + 0.8 * radius * cos(angle), y + 0.8 * radius * sin(angle))
            path.lineTo(x + 0.5 * radius * cos(angle - 0.8), y + 0.5 * radius * sin(angle - 0.8))
            path.closePath()
            g.fill(path)

            // Rays
            for (rayId in 0 until creature.raysNumber) {
                val raySignal = max(
                    creature.creatureRayData[rayId],
                    max(creature.wallRayData[rayId], creature.foodRayData[rayId])
                )
                if (creature.creatureRayData[rayId] == 0.0 && creature.wallRayData[rayId] == 0.0 && creature.foodRayData[rayId] == 0.0)
                    g.color = Color(0f, 0f, 0f, .05f)
                else
                    g.color = Color(
                        if (creature.creatureRayData[rayId] != 0.0) 1f else 0f,
                        if (creature.foodRayData[rayId] != 0.0) 0.5f else 0f,
                        if (creature.wallRayData[rayId] != 0.0) 1f else 0f,
                        (0.25 + 0.75 * raySignal).toFloat()
                    )
                val rayAngle = angle - creature.fov / 2 + creature.fov / creature.raysNumber * (rayId + 0.5)
                g.draw(Line2D.Double(
                    x + cos(rayAngle) * (radius + 2),
                    y + sin(rayAngle) * (radius + 2),
                    x + cos(rayAngle) * (radius + creature.visionDistance * (1 - raySignal) - 2),
                    y + sin(rayAngle) * (radius + creature.visionDistance * (1 - raySignal) - 2)
                ))
            }

            // Bars
            g.color = Color.CYAN.darker()
            g.fill(Rectangle2D.Double(x - radius, y + radius + 4, radius*2, 8.0))
            g.color = Color.CYAN
            g.fill(Rectangle2D.Double(x - radius, y + radius + 4, radius*2*creature.energy/creature.maxEnergy, 8.0))
            g.color = Color.GREEN.darker()
            g.fill(Rectangle2D.Double(x - radius, y + radius + 12, radius*2, 8.0))
            g.color = Color.GREEN
            g.fill(Rectangle2D.Double(x - radius, y + radius + 12, radius*2*creature.health/creature.maxHealth, 8.0))
        }

        g.color = Color(235, 119, 52)
        world.food.forEach {
            g.fill(Arc2D.Double(
                it.position.x - it.radius,
                it.position.y - it.radius,
                it.radius*2,
                it.radius*2,
                0.0,
                360.0,
                Arc2D.CHORD
            ))
        }

        if (config.showSpacePartitioning) {
            val nodesStack = LinkedList<KdTree.Node>()
            nodesStack.push(world.kdTree.root)

            while (nodesStack.isNotEmpty()) {
                val node = nodesStack.pop()

                if (!node.final) {
                    nodesStack.push(node.left)
                    nodesStack.push(node.right)
                }

                if (node.axis == KdTree.Axis.X) {
                    g.color = Color.BLUE.darker()
                    g.draw(Line2D.Double(node.splitLine, node.aabb.min.y, node.splitLine, node.aabb.max.y))
                } else {
                    g.color = Color.RED.darker()
                    g.draw(Line2D.Double(node.aabb.min.x, node.splitLine, node.aabb.max.x, node.splitLine))
                }
            }
        }

        if (selection is PhysicalBody) {
            g.color = Color.ORANGE
            g.stroke = BasicStroke(2f, 0, 0, 1f, arrayOf(5f, 5f).toFloatArray(), 0f)
            val aabb = (selection as PhysicalBody).aabb
            g.draw(Rectangle2D.Double(aabb.min.x, aabb.min.y, aabb.width, aabb.height))
        }

        g.stroke = BasicStroke(1f)
        if (selection is Creature) {
            val creature = selection as Creature
            val y = creature.position.y + creature.radius + 40
            val startX = creature.position.x - 100.0
            val width = 200.0

            for (rayId in 0 until creature.raysNumber) {
                val raySignal = max(
                    creature.creatureRayData[rayId],
                    max(creature.wallRayData[rayId], creature.foodRayData[rayId])
                )
                g.color = Color(0f, 0f, 0f, 0.5f)
                g.draw(Rectangle2D.Double(startX + rayId*width/creature.raysNumber, y, width/creature.raysNumber, 25.0))
                if (creature.creatureRayData[rayId] == 0.0 && creature.wallRayData[rayId] == 0.0 && creature.foodRayData[rayId] == 0.0)
                    g.color = Color(0f, 0f, 0f, .05f)
                else
                    g.color = Color(
                        if (creature.creatureRayData[rayId] != 0.0) 1f else 0f,
                        if (creature.foodRayData[rayId] != 0.0) 0.5f else 0f,
                        if (creature.wallRayData[rayId] != 0.0) 1f else 0f
                    )
                g.fill(Rectangle2D.Double(startX + rayId*width/creature.raysNumber, y + 25.0*(1 - raySignal), width/creature.raysNumber, 25.0*raySignal))
            }
        }

        if (System.nanoTime() - fpsCounterLastTime > 100000000) {
            fpsCounterLastFps = fpsCounterTicks.toDouble()/(System.nanoTime() - fpsCounterLastTime)*1000000000
            fpsCounterLastTime = System.nanoTime()
            fpsCounterTicks = 0
        }
        g.color = Color.BLACK
        g.transform = AffineTransform()
        g.font = Font("Arial", Font.PLAIN, 18)
        g.drawString("${String.format("%.1f", fpsCounterLastFps)} FPS", 0, 18)
        fpsCounterTicks++
    }

    fun update(delta: Double) {
        if (selection is Creature) {
            cameraX = (selection as Creature).position.x
            cameraY = (selection as Creature).position.y
            updateCameraTransform()
        }
    }

    override fun mouseWheelMoved(e: MouseWheelEvent) {
        val rotation = e.preciseWheelRotation
        val oldViewportHeight = viewportHeight
        viewportHeight *= if (rotation < 0) -1/(rotation/4 - 1) else rotation/4 + 1
        val viewportHeightDifference = oldViewportHeight - viewportHeight
        cameraX += viewportHeightDifference*(e.point.x - width/2)/height
        cameraY += viewportHeightDifference*(e.point.y - height/2)/height
        updateCameraTransform()
    }

    private fun updateCameraTransform() {
        val scale = height / viewportHeight
        cameraTransform.setToIdentity()
        cameraTransform.translate(width / 2.0, height / 2.0)
        cameraTransform.translate(-cameraX * scale, -cameraY * scale)
        cameraTransform.scale(scale, scale)
    }

    override fun componentResized(e: ComponentEvent?) = updateCameraTransform()
    override fun componentShown(e: ComponentEvent?) = updateCameraTransform()
    override fun componentMoved(e: ComponentEvent?) {}
    override fun componentHidden(e: ComponentEvent?) {}
    override fun mouseClicked(e: MouseEvent) {
        val worldPosition = cameraTransform.inverseTransform(e.point, null)
        selection = world.creatures.firstOrNull { DoublePoint(worldPosition.x, worldPosition.y).distance(it.position) < it.radius }
    }

    override fun mousePressed(e: MouseEvent) {
        isDragging = true
        lastMousePosition.x = e.x
        lastMousePosition.y = e.y
    }

    override fun mouseReleased(e: MouseEvent) { isDragging = false }

    override fun mouseEntered(e: MouseEvent) {}

    override fun mouseExited(e: MouseEvent) { isDragging = false }

    override fun mouseDragged(e: MouseEvent) {
        val scale = height / viewportHeight
        cameraX += (lastMousePosition.x - e.x)/scale
        cameraY += (lastMousePosition.y - e.y)/scale
        lastMousePosition.x = e.x
        lastMousePosition.y = e.y
        updateCameraTransform()
    }

    override fun mouseMoved(e: MouseEvent) {}
}