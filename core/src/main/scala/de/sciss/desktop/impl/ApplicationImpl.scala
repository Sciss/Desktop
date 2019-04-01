/*
 *  ApplicationImpl.scala
 *  (Desktop)
 *
 *  Copyright (c) 2013-2019 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.desktop
package impl

import swing.Swing

trait ApplicationImpl[Document] extends Application[Document] {
  private[this] lazy val _systemPrefs = Preferences.system(getClass)
  private[this] lazy val _userPrefs   = Preferences.user  (getClass)

  final def systemPrefs: Preferences = _systemPrefs
  final def userPrefs  : Preferences = _userPrefs

  private[this] val sync          = new AnyRef
  private[this] var componentMap  = Map.empty[String, Any]

  def addComponent(key: String, component: Any): Unit =
    sync.synchronized(componentMap += key -> component)

  def removeComponent(key: String): Unit =
    sync.synchronized(componentMap -= key)

  def getComponent[A](key: String): Option[A] = sync.synchronized(componentMap.get(key).asInstanceOf[Option[A]])
}

abstract class SwingApplicationImpl[Document](val name: String)
  extends ApplicationImpl[Document] with SwingApplication[Document] {

  app =>

  private[this] var _args: Array[String] = _

  protected final def args: Array[String] = _args

  def main(args: Array[String]): Unit = {
    _args = args

    sys.props("com.apple.mrj.application.apple.menu.about.name") = name

    if (Desktop.isMac) {
      sys.props("apple.laf.useScreenMenuBar") = "true"
    }
    Swing.onEDT {
      //    /* val mf = */ new MainWindowImpl {
      //      def handler = app.windowHandler
      //      title       = name
      //      front()
      //    }
      init()
      windowHandler // makes sure that a main frame is created in interal-frame mode
    }
  }

  /** Subclasses may override this to initialize the GUI on the event thread */
  protected def init(): Unit = ()

  protected def menuFactory: Menu.Root

  final protected implicit def application: SwingApplication[Document] = this

  lazy implicit val documentHandler: DocumentHandler =
    new DocumentHandlerImpl[Document]

  lazy implicit val windowHandler: WindowHandler = new WindowHandlerImpl(this, menuFactory)

  def quit(): Unit = sys.exit()
}