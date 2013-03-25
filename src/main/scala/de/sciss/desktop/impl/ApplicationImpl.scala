/*
 *  ApplicationImpl.scala
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

import swing.Swing

trait ApplicationImpl extends Application {
  private lazy val _systemPrefs = Preferences.system(getClass)
  private lazy val _userPrefs   = Preferences.user  (getClass)

  final def systemPrefs: Preferences = _systemPrefs
  final def userPrefs  : Preferences = _userPrefs

  private val sync          = new AnyRef
  private var componentMap  = Map.empty[String, Any]

  def addComponent(key: String, component: Any) {
    sync.synchronized(componentMap += key -> component)
  }

  def removeComponent(key: String) {
    sync.synchronized(componentMap -= key)
  }

  def getComponent[A](key: String): Option[A] = sync.synchronized(componentMap.get(key).asInstanceOf[Option[A]])
}

abstract class SwingApplicationImpl(val name: String) extends ApplicationImpl with SwingApplication with App {
  app =>

  sys.props("com.apple.mrj.application.apple.menu.about.name") = name

  if (Desktop.isMac) {
    sys.props("apple.laf.useScreenMenuBar") = "true"
  }
  Swing.onEDT {
    /* val mf = */ new MainWindowImpl {
      def handler = app.windowHandler
      title       = name
      front()
    }
    init()
  }

  /** Subclasses may override this to initialize the GUI on the event thread */
  protected def init() {}
  protected def menuFactory: Menu.Root

  final protected implicit def application: SwingApplication { type Document = app.Document } = this

  lazy implicit val documentHandler: DocumentHandler { type Document = app.Document } =
    new DocumentHandlerImpl[Document]

  lazy implicit val windowHandler: WindowHandler = new WindowHandlerImpl(this, menuFactory)
}