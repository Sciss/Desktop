/*
 *  Platform.scala
 *  (Desktop)
 *
 *  Copyright (c) 2013-2014 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU Lesser General Public License v3+
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.desktop

import java.io.File
import de.sciss.model.Model
import scala.swing.Image

trait Platform extends Model[Desktop.Update] {
  def revealFile     (file: File): Unit
  def moveFileToTrash(file: File): Unit

  def setDockBadge   (label: Option[String]): Unit
  def setDockImage   (image: Image         ): Unit

  def requestUserAttention (repeat    : Boolean): Unit
  def requestForeground    (allWindows: Boolean): Unit

  def setQuitHandler(test: => Boolean): Unit
}
