package space.davids_digital.neurobots.gui.util

import java.awt.Font
import java.awt.Graphics2D
import java.awt.Rectangle

/**
 * Draw a String centered in the middle of a Rectangle.
 *
 * @param g The Graphics instance.
 * @param text The String to draw.
 * @param rect The Rectangle to center the text in.
 */
fun Graphics2D.drawCenteredString(text: String, rect: Rectangle) {
    // Get the FontMetrics
    val metrics = this.getFontMetrics(font)
    // Determine the X coordinate for the text
    val x = rect.x + (rect.width - metrics.stringWidth(text)) / 2
    // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
    val y = rect.y + (rect.height - metrics.height) / 2 + metrics.ascent
    // Draw the String
    this.drawString(text, x, y)
}