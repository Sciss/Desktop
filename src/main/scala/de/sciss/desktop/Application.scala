/*
 *  Application.scala
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

trait Application {
  app =>
  type Document
  def quit(): Unit
  def name: String

  def addComponent(key: String, component: Any): Unit
  def removeComponent(key: String): Unit
  def getComponent[A](key: String): Option[A]
  def documentHandler: DocumentHandler {
    type Document = app.Document
  }

  def userPrefs: Preferences
  def systemPrefs: Preferences
}

trait SwingApplication extends Application {
  def windowHandler: WindowHandler
}