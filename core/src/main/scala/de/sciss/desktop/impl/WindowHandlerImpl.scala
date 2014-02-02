/*
 *  WindowHandlerImpl.scala
 *  (Desktop)
 *
 *  Copyright (c) 2013-2014 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU Lesser General Public License v3+
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.desktop
package impl

import java.awt.Rectangle
import javax.swing.{JInternalFrame, JDesktopPane}
import scala.collection.immutable.{IndexedSeq => Vec}

final class WindowHandlerImpl(val application: SwingApplication, val menuFactory: Menu.Root) extends WindowHandler {
  impl =>

  private var _windows = Vec.empty[Window]

  def showDialog[A](window: Option[Window], source: DialogSource[A]): A = {
 		// temporarily disable alwaysOnTop
 		val wasOnTop = if (!usesInternalFrames && usesFloatingPalettes) windows.filter { w =>
       val res = w.alwaysOnTop
       if (res) w.alwaysOnTop = false
       res
    } .toList else Nil

 		try {
 			source.show(window)
 		} finally { // make sure to restore original state
       wasOnTop.foreach(_.alwaysOnTop = true)
 		}
 	}

  def addWindow(w: Window): Unit = {
    _windows :+= w
    MainWindowImpl.add(w)
  }

  def removeWindow(w: Window): Unit = {
    val i = _windows.indexOf(w)
    if (i >= 0) _windows = _windows.patch(i, Nil, 1)
  }

  def windows: Iterator[Window] = _windows.iterator

  def usesInternalFrames  : Boolean = !Desktop.isMac
  def usesScreenMenuBar   : Boolean =  Desktop.isMac
  def usesFloatingPalettes: Boolean = true

  def mainWindow: Window = MainWindowImpl

  mainWindow.front()

  private object MainWindowImpl extends WindowStub {
    import WindowImpl._

    // protected def style = Window.Regular
    def handler = impl

    private val frame = new swing.Frame
    protected val delegate =
      Delegate.frame(this, frame, hasMenuBar = true, screen = impl.usesScreenMenuBar)

    if (Desktop.isMac) {
      makeUndecorated()
      bounds      = new Rectangle(Short.MaxValue, Short.MaxValue, 0, 0)
    } else {
      bounds      = Window.availableSpace
    }

    private val desktop: Option[JDesktopPane] =
      if (impl.usesInternalFrames) {
        val res = new JDesktopPane
        frame.peer.setContentPane(res)
        Some(res)
      } else None

    def add(w: Window): Unit =
      desktop.foreach { d =>
        w.component.peer match {
          case jif: JInternalFrame =>
            //            jif.addComponentListener(new ComponentAdapter {
            //              override def componentShown(e: ComponentEvent) {
            //                println("SHOWN")
            d.add(jif)
            //              }
            //            })
            //            println("ADD")
            //            jif.setVisible(true)
          case _ =>
        }
      }

    // handler.mainWindow = this
    closeOperation = Window.CloseIgnore
    reactions += {
      case Window.Closing(_) => application.quit()
    }
  }
}