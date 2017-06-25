/*
 *  Application.scala
 *  (Desktop)
 *
 *  Copyright (c) 2013-2017 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.desktop

import de.sciss.desktop.{DocumentHandler => DH}

trait Application {
  app =>
  type Document
  type DocumentHandler = DH {
    type Document = app.Document
  }

  def quit(): Unit
  def name: String

  def addComponent   (key: String, component: Any): Unit
  def removeComponent(key: String): Unit
  def getComponent[A](key: String): Option[A]

  def documentHandler: DocumentHandler

  def userPrefs  : Preferences
  def systemPrefs: Preferences
}

//object SwingApplication {
//
//}
trait SwingApplication extends Application {
  def windowHandler: WindowHandler
}