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

//object WindowHandlerImpl {
//  private final class DialogWindow(dialog: Dialog) extends WindowImpl {
//// 			if( modal ) fph.addModalDialog(); // this shit is necessary because java.awt.FileDialog doesn't fire windowActivated ...
//    visible = true
//// 			if( modal ) fph.removeModalDialog();
//    dispose()
//  }
//}
final class WindowHandlerImpl(val application: SwingApplication, val menu: Menu.Root) extends WindowHandler {
  private var _windows = Vector.empty[Window]

  private val isMac = sys.props("os.name").contains("Mac OS")

  def showDialog[A](window: Window, source: DialogSource[A]): A = {
 		// temporarily disable alwaysOnTop
 		val wasOnTop = if (!usesInternalFrames && usesFloatingPalettes) windows.filter { w =>
       val res = window.alwaysOnTop
       if (res) window.alwaysOnTop = false
       res
    } .toList else Nil

 		try {
 			source.show()
 		} finally { // make sure to restore original state
       wasOnTop.foreach(_.alwaysOnTop = true)
 		}
 	}

  def addWindow(w: Window) {
    _windows :+= w
  }

  def removeWindow(w: Window) {
    val i = _windows.indexOf(w)
    if (i >= 0) _windows = _windows.patch(i, Vector.empty, 1)
  }

  def windows: Iterator[Window] = _windows.iterator

  def usesInternalFrames: Boolean = false // XXX TODO

  def usesScreenMenuBar: Boolean = isMac

  def usesFloatingPalettes: Boolean = true  // XXX TODO

  def setDefaultBorrower(w: Window) {
    // XXX TODO
  }
}