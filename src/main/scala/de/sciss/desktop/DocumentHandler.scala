/*
 *  DocumentHandler.scala
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

object DocumentHandler {
  sealed trait Update[A]
  final case class Added    [A](document: A) extends Update[A]
  final case class Removed  [A](document: A) extends Update[A]
  final case class Activated[A](document: A) extends Update[A]

  type Listener[A] = PartialFunction[Update[A], Unit]
}
trait DocumentHandler {
  import DocumentHandler._

  type Document

  var activeDocument: Option[Document]
  def documents: Iterator[Document]
  def addListener   (listener: Listener[Document]): Listener[Document]
  def removeListener(listener: Listener[Document]): Unit

  /** Adds a new document to the list of handled documents. If there is currently
    * no active document, this call will also implicitly make the document the active one.
    */
  def addDocument   (document: Document): Unit
  def removeDocument(document: Document): Unit
}