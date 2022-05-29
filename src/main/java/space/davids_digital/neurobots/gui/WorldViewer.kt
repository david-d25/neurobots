package space.davids_digital.neurobots.gui

import space.davids_digital.neurobots.model.Creature
import space.davids_digital.neurobots.model.Wall
import space.davids_digital.neurobots.model.World
import java.awt.*
import java.awt.event.*
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.util.function.Consumer
import javax.swing.JPanel
import kotlin.math.cos
import kotlin.math.sin

class WorldViewer(var world: World) : JPanel(), MouseListener, MouseMotionListener, MouseWheelListener, ComponentListener {
    private var cameraX = 0.0
    private var cameraY = 0.0
    private var viewportHeight = 800.0
    private val cameraTransform = AffineTransform()
    private var lastMousePosition = Point()
    private var isDragging = false

    init {
        addMouseMotionListener(this)
        addMouseWheelListener(this)
        addMouseListener(this)
        isFocusable = true
    }

    public override fun paintComponent(graphics: Graphics) {
        super.paintComponent(graphics)
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
        world.walls.forEach(Consumer { wall: Wall ->
            g.drawLine(
                wall.pointA.x.toInt(),
                wall.pointA.y.toInt(),
                wall.pointB.x.toInt(),
                wall.pointB.y.toInt()
            )
        })

        // Creature
        world.creatures.forEach(Consumer { creature: Creature ->
            val x = creature.position.x.toInt()
            val y = creature.position.y.toInt()
            val radius = creature.radius
            val angle = creature.angle
            g.color = creature.color
            g.fillArc(
                x - radius.toInt(),
                y - radius.toInt(),
                creature.radius.toInt() * 2,
                creature.radius.toInt() * 2,
                0,
                360
            )
            g.color = creature.color.darker()
            g.drawArc(
                x - radius.toInt(),
                y - radius.toInt(),
                creature.radius.toInt() * 2,
                creature.radius.toInt() * 2,
                0,
                360
            )
            g.fillPolygon(
                intArrayOf(
                    (x + 0.5 * radius * cos(angle + 0.8)).toInt(),
                    (x + 0.8 * radius * cos(angle)).toInt(),
                    (x + 0.5 * radius * cos(angle - 0.8)).toInt()
                ),
                intArrayOf(
                    (y + 0.5 * radius * sin(angle + 0.8)).toInt(),
                    (y + 0.8 * radius * sin(angle)).toInt(),
                    (y + 0.5 * radius * sin(angle - 0.8)).toInt()
                ),
                3
            )

            // Rays
            for (rayId in 0 until creature.raysNumber) {
                val isCreatureRay = creature.creatureRayData[rayId] > creature.wallRayData[rayId]
                val rayLength = if (isCreatureRay) creature.creatureRayData[rayId] else creature.wallRayData[rayId]
                if (creature.creatureRayData[rayId] == 0.0 && creature.wallRayData[rayId] == 0.0)
                    g.color = Color(0f, 0f, 0f, .05f)
                else
                    g.color = Color(if (isCreatureRay) 1f else 0f, 0f, if (isCreatureRay) 0f else 1f, (0.25 + 0.75 * rayLength).toFloat())
                val rayAngle = angle - creature.fov / 2 + creature.fov / creature.raysNumber * (rayId + 0.5)
                g.drawLine(
                    (x + cos(rayAngle) * radius).toInt(),
                    (y + sin(rayAngle) * radius).toInt(),
                    (x + cos(rayAngle) * (radius + creature.visionDistance * (1 - rayLength))).toInt(),
                    (y + sin(rayAngle) * (radius + creature.visionDistance * (1 - rayLength))).toInt()
                )
            }
        })
    }

    fun update(delta: Double) {

    }

    override fun mouseWheelMoved(e: MouseWheelEvent) {
        viewportHeight *= if (e.preciseWheelRotation < 0) -1/(e.preciseWheelRotation/4 - 1) else e.preciseWheelRotation/4 + 1
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
    override fun mouseClicked(e: MouseEvent) {}

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