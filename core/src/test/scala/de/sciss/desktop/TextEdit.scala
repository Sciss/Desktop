package de.sciss.desktop

import de.sciss.desktop.impl.{WindowHandlerImpl, WindowImpl, SwingApplicationImpl}
import swing.{Action, Swing}
import Swing._
import java.awt
import awt.event.KeyEvent
import java.io.File
import javax.swing.text.PlainDocument
import java.awt.Color

object TextEdit extends SwingApplicationImpl("TextEdit") {
  override def quit(): Unit = {
    println("Bye bye...")
    sys.exit()
  }

  override lazy val windowHandler: WindowHandler = new WindowHandlerImpl(this, menuFactory) {
    override def usesInternalFrames: Boolean = true
  }

  private var docs = Map.empty[Document, DocumentWindow]

  private lazy val recent = RecentFiles(userPrefs("recent-docs")) { file =>
    openDocument(file)
  }

  def findDocument(file: File): Option[Document] = {
    val some = Some(file)
    documentHandler.documents.find(_.file == some)
  }

  def openDocument(file: File): Unit = {
    findDocument(file).flatMap(docs.get) match {
      case Some(window) => window.front()
      case _            => newDocument(Some(file))
    }
    recent.add(file)  // put it to the front
  }

  protected lazy val miDarkBackground = {
    import Menu._
    CheckBox("darkBg", proxy("Dark Background"))
  }

  protected lazy val menuFactory = {
    import Menu._
    import KeyStrokes._
    import scala.swing.event.Key

    Root()
      .add(Group("file", "File")
        .add(Item("new")("New" -> (menu1 + Key.N))(newDocument()))
        .add(Item("open")("Open..." -> (menu1 + Key.O)) {
          val dlg = FileDialog.open()
          dlg.show(None)
          dlg.file.foreach(openDocument)
        })
        .add(recent.menu)
        .addLine()
        .add(Item("close",  proxy("Close"      -> (menu1 + Key.W))))
        .add(Item("save",   proxy("Save"       -> (menu1 + Key.S))))
        .add(Item("saveAs", proxy("Save As..." -> (menu1 + shift + Key.S))))
      )
      .add(Group("view", "View")
        .add(miDarkBackground)
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
    win =>

    def handler = TextEdit.windowHandler
    title = document.name
    size  = (400, 200)
    file  = document.file
    component.background = Color.white

    bindMenus(
      "file.close" -> Action("Close") {
        closeDocument(document)
      },
      "file.save" -> Action("Save") {
        println("Save")
      },
      "file.saveAs" -> Action("Save As") {
        println("Save As")
      },
      "view.darkBg" -> new Action(null) {
        def apply(): Unit = {
          // println(s"Selected: ${Menu.CheckBox.selected}")
          win.component.background = if (miDarkBackground(win).selected) Color.black else Color.white
        }
      }
    )

    front()
  }

  def closeDocument(doc: Document): Unit = {
    documentHandler.removeDocument(doc)
    docs.get(doc).foreach(_.dispose())
    docs -= doc
  }

  def newDocument(file: Option[File] = None): Unit = {
    val doc   = new Document
    doc.file  = file
    documentHandler.addDocument(doc)
    val w     = new DocumentWindow(doc)
    docs     += doc -> w
  }
}