/*
 *  LogWindowImpl.scala
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
package impl

import scala.swing.{ScrollPane, Swing}
import java.io.{PrintStream, OutputStream}
import javax.swing.BorderFactory
import de.sciss.desktop

abstract class LogWindowImpl extends WindowImpl {
  frame =>

  override protected def style = Window.Auxiliary

  val log: LogPane = LogPane(rows = 24)

  @volatile private var becomeVisible = true

  private val observerOut: OutputStream = new OutputStream {
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

  private val observerPrint: PrintStream = new PrintStream(observerOut, true)
  //  {
  //    override def toString = "observerPrint"
  //  }

  //  private var obsCnt    = 0
  //  private var writeCnt  = 0
  //
  //  private def updateTitle() = title = s"obs = $obsCnt, write = $writeCnt"
  //
  //  private def incObsCnt  () = { obsCnt   += 1; updateTitle() }
  //  private def incWriteCnt() = { writeCnt += 1; updateTitle() }

  // def observe(): Unit = {
    // incObsCnt()
    System.setOut(observerPrint)
    System.setErr(observerPrint)
  // }

  // observe()
  closeOperation = desktop.Window.CloseIgnore
  reactions += {
    case Window.Closing(_) =>
      frame.visible = false
      // observe()
      becomeVisible = true
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