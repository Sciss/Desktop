/*
 *  WindowHandler.scala
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

//object WindowHandler {
//  object Preferences {
//    /** Value: Boolean stating whether internal frames within one
//      * big app frame are used. Has default value: no!<br>
//      * Node: root
//      */
//    final val keyInternalFrames = "internal-frames"
//
//    /** Value: Boolean stating whether palette windows should
//      * be floating on top and have palette decoration. Has default value: no!<br>
//      * Node: root
//      */
//    final val keyFloatingPalettes = "floating-palettes"
//
//    /** Value: Boolean stating whether to use the look-and-feel (true)
//      * or native (false) decoration for frame borders. Has default value: no!<br>
//      * Node: root
//      */
//    final val keyLookAndFeelDecoration = "laf-decoration"
//
//    final val keyIntrudingGrowBox = "intruding-growbox"
//  }
//
//  //  final val OPTION_EXCLUDE_FONT: AnyRef = "exclude-font"
//  //  final val OPTION_GLOBAL_MENUBAR: AnyRef = "global-menu"
//
//  //  def findWindow(component: Component): Option[Window] = Impl.findWindow(component)
//  //
//  //  def showDialog(dialog: Dialog)                                           { Impl.showDialog(dialog) }
//  //  def showDialog(parent: Component, dialog: Dialog)                        { Impl.showDialog(parent, dialog) }
//  //  def showDialog(parent: Component, pane: JOptionPane, title: String): Any = Impl.showDialog(parent, pane, title)
//  //  def showDialog(pane: JOptionPane, title: String): Any =                    Impl.showDialog(pane, title)
//  //  def showErrorDialog(exception: Exception, title: String)                 { Impl.showErrorDialog(exception, title) }
//  //
//  //  def showAction(w: Window): Action = Impl.showAction(w)
//}
trait WindowHandler {
  def application: SwingApplication[_]

  def addWindow   (w: Window): Unit
  def removeWindow(w: Window): Unit

  def windows: Iterator[Window]

  //  def createWindow(flags: Int): Window

  def usesInternalFrames  : Boolean
  def usesScreenMenuBar   : Boolean
  def usesFloatingPalettes: Boolean
  def usesNativeDecoration: Boolean

  def mainWindow  : Window
  def menuFactory : Menu.Root

  def showDialog[A](window: Option[Window], source: DialogSource[A]): A
}