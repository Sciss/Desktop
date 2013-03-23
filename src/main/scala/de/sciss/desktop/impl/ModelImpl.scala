/*
 *  ModelImpl.scala
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

import scala.util.control.NonFatal

trait ModelImpl[U] extends Model[U] {
  private type Listener = Model.Listener[U]
  private val sync      = new AnyRef
  private var listeners = Vector.empty[Listener]

  final protected def dispatch(update: U) {
    sync.synchronized {
      listeners.foreach { pf =>
        if (pf.isDefinedAt(update)) try {
          pf(update)
        } catch {
          case NonFatal(e) => e.printStackTrace()
        }
      }
    }
  }

  final def addListener(pf: Listener) = sync.synchronized {
    listeners :+= pf
    pf
  }

  final def removeListener(pf: Listener) { sync.synchronized {
    val idx = listeners.indexOf(pf)
    if (idx >=0 ) listeners = listeners.patch(idx, Nil, 1)
  }}
}