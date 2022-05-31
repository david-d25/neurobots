package space.davids_digital.neurobots.gui

import space.davids_digital.neurobots.world.World
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class ControlPanel(
    private val world: World
): JPanel() {
    private val startBtn = JButton("Start").also { it.addActionListener { world.paused = false } }
    private val pauseBtn = JButton("Pause").also { it.addActionListener { world.paused = true } }
    private val clearBtn = JButton("Clear area").also { it.addActionListener { world.creatures.clear() } }

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        border = EmptyBorder(10, 10, 10, 10)
        add(JPanel().also { panel ->
            panel.layout = BoxLayout(panel, BoxLayout.X_AXIS)
            panel.add(startBtn)
            panel.add(pauseBtn)
            panel.add(clearBtn)
        })
    }
}