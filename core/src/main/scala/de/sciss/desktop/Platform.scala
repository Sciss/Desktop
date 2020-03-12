/*
 *  Platform.scala
 *  (Desktop)
 *
 *  Copyright (c) 2013-2020 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.desktop

import de.sciss.file.File
import de.sciss.model.Model

import scala.concurrent.Future
import scala.swing.Image

trait Platform extends Model[Desktop.Update] {
  def revealFile     (file: File): Unit
  def moveFileToTrash(file: File): Unit

  def setDockBadge   (label: Option[String]): Unit
  def setDockImage   (image: Image         ): Unit

  def requestUserAttention (repeat    : Boolean): Unit
  def requestForeground    (allWindows: Boolean): Unit

  /** Returns `true` if the handler is natively supported, otherwise `false`. */
  def setQuitHandler       (test  : => Future[Unit]): Boolean
  /** Returns `true` if the handler is natively supported, otherwise `false`. */
  def setAboutHandler      (action: => Unit   ): Boolean
  /** Returns `true` if the handler is natively supported, otherwise `false`. */
  def setPreferencesHandler(action: => Unit   ): Boolean
}