/*
 *  DummyPlatform.scala
 *  (Desktop)
 *
 *  Copyright (c) 2013-2015 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.desktop
package impl

import de.sciss.desktop.Desktop.Update
import de.sciss.file.File
import de.sciss.model.Model

import scala.swing.Image

/** The fall back "platform" has no-ops for most of the API. */
object DummyPlatform extends Platform {
  override def toString = "DummyPlatform"

  def revealFile     (file: File): Unit = ()
  def moveFileToTrash(file: File): Unit = ()

  def addListener   (pf: Model.Listener[Update]): pf.type = pf
  def removeListener(pf: Model.Listener[Update]): Unit    = ()

  def setDockImage(image: Image         ): Unit = ()
  def setDockBadge(label: Option[String]): Unit = ()

  def requestUserAttention (repeat    : Boolean): Unit = ()
  def requestForeground    (allWindows: Boolean): Unit = ()

  def setQuitHandler       (test  : => Boolean): Boolean = false
  def setAboutHandler      (action: => Unit   ): Boolean = false
  def setPreferencesHandler(action: => Unit   ): Boolean = false
}