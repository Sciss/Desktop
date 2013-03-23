package de.sciss.desktop
package impl

trait DocumentHandlerImpl[A] extends DocumentHandler with ModelImpl[DocumentHandler.Update[A]] {
  private var _active     = Option.empty[Document]
  private var _documents  = Vector.empty[Document]
  private val sync        = new AnyRef

  final type Document = A

  override def toString = "DocumentHandler@" + hashCode().toHexString

  final def documents: Iterator[Document] = _documents.iterator

  def activeDocument: Option[Document] = _active
  def activeDocument_=(value: Option[Document]) {
    if (_active != value) {
      _active = value
      value.foreach { doc => dispatch(DocumentHandler.Activated(doc)) }
    }
  }

  def addDocument(document: Document) {
    sync.synchronized {
      _documents :+= document
      dispatch(DocumentHandler.Added(document))
      if (_active.isEmpty) {
        activeDocument = Some(document)
      }
    }
  }

  def removeDocument(document: Document) {
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
}