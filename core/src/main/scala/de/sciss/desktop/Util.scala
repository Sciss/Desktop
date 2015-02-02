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
  def centerOnScreen(w: Window): Unit = placeWindow(w, 0.5f, 0.5f, 0)

  def delay(millis: Int)(block: => Unit): Unit = {
    val timer = new Timer(millis, Swing.ActionListener(_ => block))
    timer.setRepeats(false)
    timer.start()
  }

  def fixSize(c: Component): Unit = {
    val d = c.preferredSize
    c.preferredSize = d
    c.minimumSize   = d
    c.maximumSize   = d
  }

  def fixWidth(c: Component, width: Int = -1): Unit = {
    val w         = if (width < 0) c.preferredSize.width else width
    val min       = c.minimumSize
    val max       = c.maximumSize
    min.width     = w
    max.width     = w
    c.minimumSize = min
    c.maximumSize = max
  }

  //  def findWindow(c: Component): Option[Window] = {
  //    @tailrec def loop(p: JComponent): Option[Window] =
  //      p.getClientProperty(WindowImpl.WindowKey) match {
  //        case f: Window => Some(f)
  //        case _ => c.peer.getParent match {
  //          case pp: JComponent => loop(pp)
  //          case _ => None
  //        }
  //      }
  //
  //    loop(c.peer)
  //  }

  def maximumWindowBounds: Rectangle = {
    val ge  = GraphicsEnvironment.getLocalGraphicsEnvironment
    ge.getMaximumWindowBounds
  }

  def placeWindow(w: Window, horizontal: Float, vertical: Float, padding: Int): Unit = {
    val bs  = maximumWindowBounds
    val b   = w.size
    val x   = (horizontal * (bs.width  - padding * 2 - b.width )).toInt + bs.x + padding
    val y   = (vertical   * (bs.height - padding * 2 - b.height)).toInt + bs.y + padding
    w.location = new Point(x, y)
  }
}