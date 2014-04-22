/*
 *  LogPaneImpl.scala
 *  (Desktop)
 *
 *  Copyright (c) 2013-2014 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.desktop
package impl

import scala.swing._
import scala.util.control.NonFatal
import java.io.{PrintStream, OutputStream, Writer}
import scala.swing.event.MousePressed
import scala.swing.event.MouseReleased
import scala.swing.ScrollPane.BarPolicy
import javax.swing.{AbstractAction, JPopupMenu}
import java.awt.event.ActionEvent

class LogPaneImpl(rows0: Int, cols0: Int) extends LogPane {
  pane =>

  override def toString = "LogPane@" + hashCode.toHexString

  private val textPane: TextArea = new TextArea(rows0, cols0) {
    me =>

    private var totalLength = 0

    // setFont(Helper.createFont(config.font))
    editable  = false
    lineWrap  = true
    // setBackground(config.style.background)
    // setForeground(config.style.foreground)

    listenTo(mouse.clicks)
    reactions += {
      case MousePressed (_, p, _, _, true) => showPopup(p)
      case MouseReleased(_, p, _, _, true) => showPopup(p)
    }

    private def showPopup(p: Point): Unit = popup.show(me.peer, p.x, p.y)

    override def append(str: String): Unit = {
      super.append(str)
      totalLength += str.length
      updateCaret()
    }

    override def text_=(str: String): Unit = {
      super.text_=(str)
      totalLength = if (str == null) 0 else str.length
    }

    private def updateCaret(): Unit =
      try {
        caret.position = math.max(0, totalLength - 1)
      }
      catch {
        case NonFatal(_) => /* ignore */
      }
  }

  // ---- Writer ----
  val writer: Writer = new Writer {
    override def toString = pane.toString + ".writer"

    def close() = ()

    def flush() = ()

    def write(ch: Array[Char], off: Int, len: Int): Unit = {
      val str = new String(ch, off, len)
      textPane.append(str)
    }
  }

  // ---- OutputStream ----
  val outputStream: OutputStream = new OutputStream {
    override def toString = pane.toString + ".outputStream"

    override def write(b: Array[Byte], off: Int, len: Int): Unit = {
      val str = new String(b, off, len)
      textPane.append(str)
    }

    def write(b: Int): Unit = write(Array(b.toByte), 0, 1)
  }

  private val printStream = new PrintStream(outputStream, true)

  val component: ScrollPane = new ScrollPane(textPane) {
    verticalScrollBarPolicy   = BarPolicy.Always
    horizontalScrollBarPolicy = BarPolicy.Never
  }

  private val popup = {
    val p = new JPopupMenu()
    p.add(new AbstractAction("Clear All") {
      override def actionPerformed(e: ActionEvent): Unit = clear()
    })
    p
  }

  def clear(): Unit = textPane.text = null

  def makeDefault(error: Boolean): this.type = {
    // Console.setOut(outputStream)
    System.setOut(printStream)
    // if (error) Console.setErr(outputStream)
    if (error) System.setErr(printStream)
    this
  }

  def lineWrap: Boolean = textPane.lineWrap
  def lineWrap_=(value: Boolean): Unit = {
    textPane.lineWrap = value
    component.horizontalScrollBarPolicy = if (value) BarPolicy.Never else BarPolicy.Always
  }

  def background: Color = textPane.background
  def background_=(value: Color): Unit = textPane.background = value

  def foreground: Color = textPane.foreground
  def foreground_=(value: Color): Unit = textPane.foreground = value

  def font: Font = textPane.font
  def font_=(value: Font): Unit = textPane.font = value

  def rows: Int = textPane.rows
  def rows_=(value: Int): Unit = textPane.rows = value

  def columns: Int = textPane.columns
  def columns_=(value: Int): Unit = textPane.columns = value
}
