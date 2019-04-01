/*
 *  Application.scala
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

import de.sciss.desktop.{DocumentHandler => DH}

trait Application[Document] {
  app =>

  type DocumentHandler = DH[Document]

  def quit(): Unit
  def name: String

  def addComponent   (key: String, component: Any): Unit
  def removeComponent(key: String): Unit
  def getComponent[A](key: String): Option[A]

  def documentHandler: DocumentHandler

  def userPrefs  : Preferences
  def systemPrefs: Preferences
}

trait SwingApplication[Document] extends Application[Document] {
  def windowHandler: WindowHandler
}

trait ApplicationProxy[Document, Repr <: Application[Document]] extends Application[Document] { me =>
  private[this] var _peer: Repr = _

  private[this] val sync = new AnyRef

  @inline private[this] def _requireInitialized(): Unit =
    if (_peer == null) throw new IllegalStateException("Application not yet initialized")

  final protected def requireInitialized(): Unit = _requireInitialized()

  final protected def peer: Repr = _peer

  final def init(peer: Repr): Unit = sync.synchronized {
    if (me._peer != null) throw new IllegalStateException("Trying to initialize application twice")
    me._peer = peer
  }

  final def name: String = {
    _requireInitialized()
    _peer.name
  }

  final def userPrefs: Preferences = {
    _requireInitialized()
    _peer.userPrefs
  }

  final def systemPrefs: Preferences = {
    _requireInitialized()
    _peer.systemPrefs
  }

  final def documentHandler: DocumentHandler = {
    _requireInitialized()
    _peer.documentHandler
  }

  final def quit(): Unit = {
    _requireInitialized()
    _peer.quit()
  }

  final def getComponent[A](key: String): Option[A] = {
    _requireInitialized()
    _peer.getComponent(key)
  }

  final def addComponent(key: String, component: Any): Unit = {
    _requireInitialized()
    _peer.addComponent(key, component)
  }

  final def removeComponent(key: String): Unit = {
    _requireInitialized()
    _peer.removeComponent(key)
  }
}

trait SwingApplicationProxy[Document, Repr <: SwingApplication[Document]]
  extends SwingApplication[Document] with ApplicationProxy[Document, Repr] {
  me =>

  final def windowHandler: WindowHandler = {
    requireInitialized()
    peer.windowHandler
  }
}