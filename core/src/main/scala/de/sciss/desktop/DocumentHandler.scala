/*
 *  DocumentHandler.scala
 *  (Desktop)
 *
 *  Copyright (c) 2013-2014 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU Lesser General Public License v2.1+
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

  var activeDocument: Option  [Document]
  def documents     : Iterator[Document]

  // note: cannot mix in `Model[Update[A]]`, because `A` is `Document`.
  def addListener   (listener: Listener[Document]): Listener[Document]
  def removeListener(listener: Listener[Document]): Unit

  /** Adds a new document to the list of handled documents. If there is currently
    * no active document, this call will also implicitly make the document the active one.
    */
  def addDocument   (document: Document): Unit
  def removeDocument(document: Document): Unit
}