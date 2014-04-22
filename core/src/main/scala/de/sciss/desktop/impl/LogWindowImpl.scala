/*
 *  LogWindowImpl.scala
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

import scala.swing.{ScrollPane, Swing}
import java.io.OutputStream
import javax.swing.BorderFactory
import scala.swing.event.WindowClosing
import de.sciss.desktop

abstract class LogWindowImpl extends WindowImpl {
  frame =>

  override protected def style = Window.Auxiliary

  val log: LogPane = LogPane(rows = 24)

  private val observer: OutputStream = new OutputStream {
    override def write(b: Array[Byte], off: Int, len: Int): Unit = {
      log.makeDefault()               // detaches this observer
      log.outputStream.write(b, off, len)
      Swing.onEDT(frame.front())      // there we go
    }

    def write(b: Int): Unit = {
      val arr = new Array[Byte](1)
      arr(0)  = b.toByte
      write(arr, 0, 1)
    }
  }

  def observe(): Unit = {
    Console.setOut(observer)
    Console.setErr(observer)
  }

  observe()
  closeOperation = desktop.Window.CloseIgnore
  reactions += {
    case WindowClosing(_) =>
      frame.visible = false
      observe()
  }

  contents = new ScrollPane {
    contents  = log.component
    border    = BorderFactory.createEmptyBorder()
  }

  title   = "Log"
  pack()
  // import LogWindow._
  // GUI.placeWindow(frame, horizontal = horizontalPlacement, vertical = verticalPlacement, padding = placementPadding)
}