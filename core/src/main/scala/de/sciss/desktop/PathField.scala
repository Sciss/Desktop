/*
 *  PathField.scala
 *  (Desktop)
 *
 *  Copyright (c) 2013-2019 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.desktop

import java.awt.Paint
import java.io.File

import javax.swing.{BoxLayout, JComponent, JPanel}

import scala.swing.event.{EditDone, ValueChanged}
import scala.swing.{Button, Component, TextField}

class PathField extends Component {
  // they have to be lazy because super class refers to them in `peer`
  private[this] lazy val tx = new TextFieldWithPaint(24)
  private[this] lazy val bt = new PathButton

  /** The default mode is `Open`. */
  def mode: FileDialog.Mode = bt.mode

  def mode_=(value: FileDialog.Mode): Unit = bt.mode = value

  def textField: TextField  = tx

  def button   : Button     = bt  // should we expose PathButton?

  def title: String = bt.title

  def title_=(value: String): Unit = bt.title = value

  def accept: File => Option[File] = bt.accept

  def accept_=(value: File => Option[File]): Unit = bt.accept = value

  def value: File = bt.value

  /** Does not fire */
  def value_=(f: File): Unit = {
    bt.value  = f
    tx.text   = f.getPath
  }

  /** Treats empty file as `None` */
  def valueOption: Option[File] = bt.valueOption

  def valueOption_=(opt: Option[File]): Unit = value = opt.getOrElse(new File(""))

  /** Custom paint on top of the text field.
    * Use a low alpha value (e.x. 48) to make the underlying widget still shine through.
    */
  def paint: Option[Paint] = tx.paint

  def paint_=(value: Option[Paint]): Unit = tx.paint = value

  override def tooltip_=(value: String): Unit = {
    super.tooltip_=(value)
    tx   .tooltip = value
  }

  override def enabled_=(value: Boolean): Unit = {
    super.enabled_=(value)
    tx   .enabled = value
    bt   .enabled = value
  }

  override def requestFocus()         : Unit    = tx.requestFocus()
  override def requestFocusInWindow() : Boolean = tx.requestFocusInWindow()

  // ---- constructor ----

  tx.listenTo(tx)
  tx.reactions += {
    case EditDone(_) =>
      val newValue = new File(tx.text)
      if (newValue != value) {
        bt.value = newValue
        fire()
      }
  }
  bt.reactions += {
    case ValueChanged(_) =>
      tx.text = bt.value.getPath
      fire()
  }

  // ---- private and impl ----

  private def fire(): Unit =
    publish(new ValueChanged(this))

  override lazy val peer: JComponent =
    new JPanel(null) with SuperMixin {
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS))

      override def getBaseline(width: Int, height: Int): Int = {
        val res = tx.peer.getBaseline(width, height)
        res + tx.peer.getY
      }

      add(tx.peer)
      add(bt.peer)
    }
}