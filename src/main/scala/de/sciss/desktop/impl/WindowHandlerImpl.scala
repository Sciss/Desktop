/*
 *  WindowHandlerImpl.scala
 *  (Desktop)
 *
 *  Copyright (c) 2013 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU General Public License
 *	as published by the Free Software Foundation; either
 *	version 2, june 1991 of the License, or (at your option) any later version.
 *
 *	This software is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *	General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public
 *	License (gpl.txt) along with this software; if not, write to the Free Software
 *	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.desktop
package impl

import java.awt.Rectangle
import javax.swing.{JInternalFrame, JDesktopPane}
import java.awt.event.{ComponentEvent, ComponentAdapter}

//object WindowHandlerImpl {
//  private final class DialogWindow(dialog: Dialog) extends WindowImpl {
//// 			if( modal ) fph.addModalDialog(); // this shit is necessary because java.awt.FileDialog doesn't fire windowActivated ...
//    visible = true
//// 			if( modal ) fph.removeModalDialog();
//    dispose()
//  }
//}
final class WindowHandlerImpl(val application: SwingApplication, val menuFactory: Menu.Root) extends WindowHandler {
  hndl =>

  private var _windows = Vector.empty[Window]

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

  def addWindow(w: Window) {
    _windows :+= w
    MainWindowImpl.add(w)
  }

  def removeWindow(w: Window) {
    val i = _windows.indexOf(w)
    if (i >= 0) _windows = _windows.patch(i, Vector.empty, 1)
  }

  def windows: Iterator[Window] = _windows.iterator

  def usesInternalFrames  : Boolean = !Desktop.isMac
  def usesScreenMenuBar   : Boolean =  Desktop.isMac
  def usesFloatingPalettes: Boolean = true

//  private var _mainWindow: Window = null
  def mainWindow: Window = MainWindowImpl
//  def mainWindow_=(value: Window) {
//    if (_mainWindow != null) throw new IllegalStateException("Main window has already been registered")
//    _mainWindow = value
//  }

  mainWindow.front()

  private object MainWindowImpl extends WindowStub {
    import WindowImpl._

    protected def style = Window.Regular
    def handler = hndl

    private val frame = new swing.Frame
    protected val delegate =
      Delegate.frame(this, frame, hasMenuBar = true, screen = handler.usesScreenMenuBar)

    if (Desktop.isMac) {
      makeUndecorated()
      bounds      = new Rectangle(Short.MaxValue, Short.MaxValue, 0, 0)
    } else {
      bounds      = Window.availableSpace
    }

    private val desktop: Option[JDesktopPane] = {
      if (handler.usesInternalFrames) {
        val res = new JDesktopPane
        frame.peer.setContentPane(res)
        Some(res)
      } else None
    }

    def add(w: Window) {
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
    }

    // handler.mainWindow = this
    closeOperation = Window.CloseIgnore
    reactions += {
      case Window.Closing(_) => application.quit()
    }
  }
}