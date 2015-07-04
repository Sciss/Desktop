/*
 *  Util.scala
 *  (Desktop)
 *
 *  Copyright (c) 2013-2015 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.desktop

import java.awt.GraphicsEnvironment
import javax.swing.Timer

import scala.swing.{Point, Rectangle, Component, Swing}

object Util {
  /** Positions a window on the center of the default screen device. */
  def centerOnScreen(w: Window): Unit = placeWindow(w, 0.5f, 0.5f, 0)

  /** Executes a code block once after a given delay on the event-dispatch-thread. */
  def delay(millis: Int)(block: => Unit): Unit = {
    val timer = new Timer(millis, Swing.ActionListener(_ => block))
    timer.setRepeats(false)
    timer.start()
  }

  /** Sets a component's minimum and maximum size to match the preferred size. */
  def fixSize(c: Component): Unit = {
    val d = c.preferredSize
    c.preferredSize = d
    c.minimumSize   = d
    c.maximumSize   = d
  }

  /** Sets the minimum and maximum width of a component.
    *
    * @param width  the width to use or `-1` to query the preferred width instead.
    */
  def fixWidth(c: Component, width: Int = -1): Unit = {
    val w         = if (width < 0) c.preferredSize.width else width
    val min       = c.minimumSize
    val max       = c.maximumSize
    min.width     = w
    max.width     = w
    c.minimumSize = min
    c.maximumSize = max
  }

  /** Returns the maximum bounds a window should have on the default screen device. */
  def maximumWindowBounds: Rectangle = {
    val ge  = GraphicsEnvironment.getLocalGraphicsEnvironment
    ge.getMaximumWindowBounds
  }

  /** Positions a window on the default screen device.
    *
    * @param horizontal the horizontal placement from 0.0 (left-most) to 1.0 (right-most)
    * @param vertical   the vertical placement from 0.0 (top-most) to 1.0 (bottom-most)
    * @param padding    additional padding in pixels from the screen's margins. For example,
    *                   if horizontal is `1.0` and padding is `40`, the window will be
    *                   placed so that its right border is spaced 40 pixels from the right
    *                   margin of the screen.
    */
  def placeWindow(w: Window, horizontal: Float, vertical: Float, padding: Int): Unit = {
    val bs  = maximumWindowBounds
    val b   = w.size
    val x   = (horizontal * (bs.width  - padding * 2 - b.width )).toInt + bs.x + padding
    val y   = (vertical   * (bs.height - padding * 2 - b.height)).toInt + bs.y + padding
    w.location = new Point(x, y)
  }
}