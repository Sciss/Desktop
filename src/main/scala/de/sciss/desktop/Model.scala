/*
 *  Model.scala
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

object Model {
  type Listener[U] = PartialFunction[U, Unit]
}
trait Model[U] {
  /**
   * Registers a listener for updates from the model.
   * A listener is simply a partial function which receives instances of `U`. Therefore
   * the listener can decide with pattern match cases which updates it wants to handle.
   *
   * Example:
   * {{{
   *   m.addListener {
   *     case NcviewSync.Open(path) => ...
   *   }
   * }}}
   *
   * __Note:__ If the listener should be removed at some point, it is important to store it somewhere:
   *
   * {{{
   *   val l: NcviewSync.Listener = { case NcviewSync.Open(path) => ... }
   *   m.addListener(l)
   *   ...
   *   m.removeListener(l)
   * }}}
   */
  def addListener(pf: Model.Listener[U]): Model.Listener[U]
  /**
   * Unregisters a listener for updates from the model.
   */
  def removeListener(pf: Model.Listener[U]): Unit
}