/*
 *  TextFieldWithPaint.scala
 *  (Desktop)
 *
 *  Copyright (c) 2013-2018 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.desktop

import java.awt.{Color, Paint}

import scala.swing.{Graphics2D, TextField}

object TextFieldWithPaint {
  val RedOverlay    = new Color(0xFF, 0x00, 0x00, 0x2F)
  val GreenOverlay  = new Color(0x00, 0xFF, 0x00, 0x2F)
  val BlueOverlay   = new Color(0x00, 0x00, 0xFF, 0x2F)
}
/** Extends `TextField` with a `paint` getter/setter for adding a tone. */
class TextFieldWithPaint(text0: String, columns0: Int) extends TextField(text0, columns0) {
  def this(text: String) = this(text, 0)
  def this(columns: Int) = this("", columns)
  def this() = this("")

  private[this] var _paint = Option.empty[Paint]

  /** Custom paint on top of the text field.
    * Use a low alpha value (e.x. 48) to make the underlying widget still shine through.
    */
  def paint: Option[Paint] = _paint

  def paint_=(value: Option[Paint]): Unit = if (_paint != value) {
    _paint = value
    repaint()
  }

  override protected def paintComponent(g: Graphics2D): Unit = {
    super.paintComponent(g)

    _paint match {
      case Some(pnt) =>
        val p       = peer
        val w       = p.getWidth
        val h       = p.getHeight
        val op      = g.getPaint
        g.setPaint(pnt)
        g.fillRect(1, 1, w - 2, h - 2)
        g.setPaint(op)

      case None =>
    }
  }
}
