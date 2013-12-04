/*
 *  WindowHandler.scala
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

object WindowHandler {
  object Preferences {
    /** Value: Boolean stating whether internal frames within one
      * big app frame are used. Has default value: no!<br>
      * Node: root
      */
    final val keyInternalFrames = "internalframes"

    /** Value: Boolean stating whether palette windows should
      * be floating on top and have palette decoration. Has default value: no!<br>
      * Node: root
      */
    final val keyFloatingPalettes = "floatingpalettes"

    /** Value: Boolean stating whether to use the look-and-feel (true)
      * or native (false) decoration for frame borders. Has default value: no!<br>
      * Node: root
      */
    final val keyLookAndFeelDecoration = "lafdecoration"

    final val keyIntrudingGrowBox = "intrudinggrowbox"
  }

  //  final val OPTION_EXCLUDE_FONT: AnyRef = "excludefont"
  //  final val OPTION_GLOBAL_MENUBAR: AnyRef = "globalmenu"

  //  def findWindow(component: Component): Option[Window] = Impl.findWindow(component)
  //
  //  def showDialog(dialog: Dialog)                                           { Impl.showDialog(dialog) }
  //  def showDialog(parent: Component, dialog: Dialog)                        { Impl.showDialog(parent, dialog) }
  //  def showDialog(parent: Component, pane: JOptionPane, title: String): Any = Impl.showDialog(parent, pane, title)
  //  def showDialog(pane: JOptionPane, title: String): Any =                    Impl.showDialog(pane, title)
  //  def showErrorDialog(exception: Exception, title: String)                 { Impl.showErrorDialog(exception, title) }
  //
  //  def showAction(w: Window): Action = Impl.showAction(w)
}
trait WindowHandler {
  def application: SwingApplication

  def addWindow   (w: Window): Unit
  def removeWindow(w: Window): Unit

  def windows: Iterator[Window]

  //  def createWindow(flags: Int): Window

  def usesInternalFrames: Boolean
  def usesScreenMenuBar: Boolean
  def usesFloatingPalettes: Boolean

  def mainWindow: Window
  def menuFactory: Menu.Root

  def showDialog[A](window: Option[Window], source: DialogSource[A]): A
}