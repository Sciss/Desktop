package de.sciss.desktop

import impl.{WindowImpl, SwingApplicationImpl, MainWindowImpl}
import swing.{Action, Swing}
import Swing._
import java.awt
import awt.FileDialog
import awt.event.KeyEvent
import java.io.File
import javax.swing.text.PlainDocument

object TextEdit extends SwingApplicationImpl("TextEdit") {
  def quit() {
    println("Bye bye...")
    sys.exit()
  }

  protected lazy val menuFactory = {
    import Menu._
    import KeyStrokes._
    import KeyEvent._
    Root()
      .add(Group("file", "File")
        .add(Item("new")("New" -> (menu1 + VK_N))(newDocument()))
        .add(Item("open")("Open..." -> (menu1 + VK_O)) {
          val dlg = new FileDialog(null: awt.Frame, "Open")
          dlg.setVisible(true)
          val f = if (dlg.getFile == null) None else Some(new File(dlg.getDirectory, dlg.getFile))
          println(s"Result: $f")
        })
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
    val handler = TextEdit.windowHandler
    protected def style = Window.Regular
    title = document.name
    size = (400, 200)

    bindMenus(
      "file.close" -> Action("Close") {
        dispose()
        documentHandler.removeDocument(document)
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

  def newDocument() {
    val doc = new Document
    documentHandler.addDocument(doc)
    val w   = new DocumentWindow(doc)
  }
}