/*
 *  MacJava9Platform.scala
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

package de.sciss.desktop.impl

import com.apple.eawt

import scala.swing.Image

/** The Mac platform using Java 9 API combined with reduced eawt API. */
object MacJava9Platform extends Java9Platform {
  override def toString = "MacJava9Platform"

  private lazy val app = eawt.Application.getApplication

  override def setDockBadge(label: Option[String]): Unit = app.setDockIconBadge(label.orNull)
  override def setDockImage(image: Image         ): Unit = app.setDockIconImage(image)

  override def requestUserAttention (repeat    : Boolean): Unit = app.requestUserAttention(repeat)
}
