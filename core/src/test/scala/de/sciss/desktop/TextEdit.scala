package de.sciss.desktop

import impl.{WindowImpl, SwingApplicationImpl}
import swing.{Action, Swing}
import Swing._
import java.awt
import awt.event.KeyEvent
import java.io.File
import javax.swing.text.PlainDocument

object TextEdit extends SwingApplicationImpl("TextEdit") {
  override def quit() {
    println("Bye bye...")
    sys.exit()
  }

  private var docs = Map.empty[Document, DocumentWindow]

  private lazy val recent = RecentFiles(userPrefs("recent-docs")) { file =>
    openDocument(file)
  }

  def findDocument(file: File): Option[Document] = {
    val some = Some(file)
    documentHandler.documents.find(_.file == some)
  }

  def openDocument(file: File) {
    findDocument(file).flatMap(docs.get) match {
      case Some(window) => window.front()
      case _            => newDocument(Some(file))
    }
    recent.add(file)  // put it to the front
  }

  protected lazy val menuFactory = {
    import Menu._
    import KeyStrokes._
    import KeyEvent._

    Root()
      .add(Group("file", "File")
        .add(Item("new")("New" -> (menu1 + VK_N))(newDocument()))
        .add(Item("open")("Open..." -> (menu1 + VK_O)) {
          val dlg = FileDialog.open()
          dlg.show(None)
          dlg.file.foreach(openDocument)
        })
        .add(recent.menu)
        .addLine()
        .add(Item("close",  proxy("Close"      -> (menu1 + VK_W))))
        .add(Item("save",   proxy("Save"       -> (menu1 + VK_S))))
        .add(Item("saveAs", proxy("Save As..." -> (menu1 + shift + VK_S))))
      )
  }

  class Document {
    def name: String = file match {
      case Some(f) =>
        val n = f.getName
        val i = n.lastIndexOf('.')
        if (i < 0) n else n.substring(0, i)
      case _ => "Untitled"
    }
    var file  = Option.empty[File]
    val peer  = new PlainDocument()
  }

  private class DocumentWindow(document: Document) extends WindowImpl {
    def handler = TextEdit.windowHandler
    title = document.name
    size  = (400, 200)
    file  = document.file

    bindMenus(
      "file.close" -> Action("Close") {
        closeDocument(document)
      },
      "file.save" -> Action("Save") {
        println("Save")
      },
      "file.saveAs" -> Action("Save As") {
        println("Save As")
      }
    )

    front()
  }

  def closeDocument(doc: Document) {
    documentHandler.removeDocument(doc)
    docs.get(doc).foreach(_.dispose())
    docs -= doc
  }

  def newDocument(file: Option[File] = None) {
    val doc   = new Document
    doc.file  = file
    documentHandler.addDocument(doc)
    val w     = new DocumentWindow(doc)
    docs     += doc -> w
  }
}