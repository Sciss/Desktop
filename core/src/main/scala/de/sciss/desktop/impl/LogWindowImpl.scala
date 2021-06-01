/*
 *  LogWindowImpl.scala
 *  (Desktop)
 *
 *  Copyright (c) 2013-2021 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.desktop.impl

import java.io.{OutputStream, PrintStream}

import de.sciss.desktop
import de.sciss.desktop.{LogPane, Window}

import scala.swing.Swing

abstract class LogWindowImpl extends WindowImpl {
  frame =>

  override protected def style /* : Window.Style */ = Window.Auxiliary

  val log: LogPane = LogPane(rows = 24)

  @volatile private[this] var becomeVisible = true

  /** Hides the window so that it will become
    * visible again when new text is printed.
    */
  def hide(): Unit = {
    frame.visible = false
    becomeVisible = true
  }

  private[this] val observerOut: OutputStream = new OutputStream {
    override def toString = "observerOut"

    override def write(b: Array[Byte], off: Int, len: Int): Unit = {
      // incWriteCnt()
      // log.makeDefault()                     // detaches this observer
      log.outputStream.write(b, off, len)
      if (becomeVisible) {
        becomeVisible = false
        Swing.onEDT {
          frame.visible = true
        }
      }
    }

    def write(b: Int): Unit = {
      val arr = new Array[Byte](1)
      arr(0)  = b.toByte
      write(arr, 0, 1)
    }
  }

  private[this] val observerPrint: PrintStream = new PrintStream(observerOut, true)

  System.setOut(observerPrint)
  System.setErr(observerPrint)

  closeOperation = desktop.Window.CloseIgnore
  reactions += {
    case Window.Closing(_) => hide()
  }

  contents = log.component

  title   = "Log"
  pack()
}