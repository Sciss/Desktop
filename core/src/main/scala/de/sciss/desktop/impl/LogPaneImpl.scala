/*
 *  LogPaneImpl.scala
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
package impl

import java.io.{OutputStream, PrintStream, Writer}

import de.sciss.swingplus.PopupMenu

import scala.swing.ScrollPane.BarPolicy
import scala.swing.event.{Key, MousePressed, MouseReleased}
import scala.swing.{Action, Color, Font, MenuItem, Point, ScrollPane, TextArea}
import scala.util.control.NonFatal

class LogPaneImpl(rows0: Int, cols0: Int) extends LogPane {
  pane =>

  override def toString = s"LogPane@${hashCode.toHexString}"

  private[this] val textPane: TextArea = new TextArea(rows0, cols0) {
    me =>

    private[this] var totalLength = 0

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

    private def showPopup(p: Point): Unit = popup.show(me, p.x, p.y)

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
    override def toString = s"$pane.writer"

    def close(): Unit = ()

    def flush(): Unit = ()

    def write(ch: Array[Char], off: Int, len: Int): Unit = {
      val str = new String(ch, off, len)
      textPane.append(str)
    }
  }

  // ---- OutputStream ----
  val outputStream: OutputStream = new OutputStream {
    override def toString = s"$pane.outputStream"

    override def write(b: Array[Byte], off: Int, len: Int): Unit = {
      val str = new String(b, off, len)
      textPane.append(str)
    }

    def write(b: Int): Unit = write(Array(b.toByte), 0, 1)
  }

  private[this] val printStream: PrintStream = new PrintStream(outputStream, true)
  //  {
  //    override def toString = s"$pane.printStream"
  //  }

  val component: ScrollPane = new ScrollPane(textPane) {
    peer.putClientProperty("styleId", "undecorated")
    verticalScrollBarPolicy   = BarPolicy.Always
    horizontalScrollBarPolicy = BarPolicy.Never
  }

  private[this] val popup: PopupMenu = {
    val res       = new PopupMenu
    // val map       = textPane.peer.getKeymap
    //    val copy      = textPane.peer.getActionMap.get(DefaultEditorKit.copyAction)
    //    val selectAll = textPane.peer.getActionMap.get(DefaultEditorKit.selectAllAction)

    res.contents += new MenuItem(new Action("Copy") {
      def apply(): Unit = textPane.copy()
      accelerator = Some(KeyStrokes.menu1 + Key.C)
    })
    res.contents += new MenuItem(new Action("Select All") {
      def apply(): Unit = textPane.selectAll()
      // accelerator = Option(selectAll).flatMap(map.getKeyStrokesForAction(_).headOption)
      accelerator = Some(KeyStrokes.menu1 + Key.A)
    })
    res.contents += new MenuItem(new Action("Clear All") {
      def apply(): Unit = clear()
      // accelerator = Some(KeyStrokes.menu1 + KeyStrokes.shift + Key.P)
    })
    res
  }

  def clear(): Unit = textPane.text = null

  def makeDefault(error: Boolean): this.type = {
    System.setOut(printStream)
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
