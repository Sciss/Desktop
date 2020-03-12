/*
 *  DocumentHandlerImpl.scala
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
package impl

import de.sciss.model.impl.ModelImpl

class DocumentHandlerImpl[A] extends DocumentHandler[A] with ModelImpl[DocumentHandler.Update[A]] {
  private[this] var _active     = Option.empty[Document]
  private[this] var _documents  = Vector.empty[Document]
  private[this] val sync        = new AnyRef

  final type Document = A

  override def toString = s"DocumentHandler@${hashCode().toHexString}"

  final def documents: Iterator[Document] = _documents.iterator

  def activeDocument: Option[Document] = _active
  def activeDocument_=(value: Option[Document]): Unit =
    if (_active != value) {
      _active = value
      value.foreach { doc => dispatch(DocumentHandler.Activated(doc)) }
    }

  def addDocument(document: Document): Unit =
    sync.synchronized {
      _documents :+= document
      dispatch(DocumentHandler.Added(document))
      if (_active.isEmpty) {
        activeDocument = Some(document)
      }
    }

  def removeDocument(document: Document): Unit =
    sync.synchronized {
      val i = _documents.indexOf(document)
      if (i < 0) throw new IllegalArgumentException(s"Document not found: $document")
      _documents = _documents.patch(i, Vector.empty, 1)
      if (_active == Some(document)) {
        activeDocument = None
      }
      dispatch(DocumentHandler.Removed(document))
    }
}