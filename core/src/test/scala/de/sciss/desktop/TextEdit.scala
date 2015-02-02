package de.sciss.desktop

import java.awt.Color
import java.io.IOException
import javax.swing.text.PlainDocument

import com.alee.laf.WebLookAndFeel
import de.sciss.desktop.impl.{SwingApplicationImpl, WindowHandlerImpl, WindowImpl}
import de.sciss.file._

import scala.swing.Swing._
import scala.swing.{FlowPanel, Label, ScrollPane, Action, TextArea}

object TextEdit extends SwingApplicationImpl("TextEdit") { app =>
  val USE_WEBLAF = true

  override protected def init(): Unit = if (USE_WEBLAF) WebLookAndFeel.install()

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
    import de.sciss.desktop.Menu._
    CheckBox("darkBg", proxy("Dark Background"))
  }

  protected lazy val menuFactory = {
    import de.sciss.desktop.KeyStrokes._
    import de.sciss.desktop.Menu._

    import scala.swing.event.Key

    val root = Root()
      .add(Group("file", "File")
        .add(Item("new")("New" -> (menu1 + Key.N))(newDocument()))
        .add(Item("open")("Open..." -> (menu1 + Key.O)) {
          val dlg = FileDialog.open(init = baseDirectoryPrefs.get)
          dlg.show(None)
          dlg.file.foreach(openDocument)
        })
        .add(recent.menu)
        .addLine()
        .add(Item("close",  proxy("Close"      -> (menu1 + Key.W))))
        .add(Item("save",   proxy("Save"       -> (menu1 + Key.S))))
        .add(Item("saveAs", proxy("Save As..." -> (menu1 + shift + Key.S))))
      )

    val miPrefs = Menu.Item.Preferences(app)(showPreferences())
    if (miPrefs.visible) root.add(Group("edit", "Edit").add(miPrefs))

    root.add(Group("view", "View")
        .add(Item("find-window", proxy("Find Window")))
        .add(miDarkBackground)
      )
    root
  }

  private lazy val baseDirectoryPrefs = userPrefs[File]("base-directory")

  def showPreferences(): Unit = {
    import PrefsGUI._
    val title = "Base Directory"
    val lb    = new Label(s"$title:")
    val pf    = pathField1(baseDirectoryPrefs, userHome, title, mode = FileDialog.Folder)
    OptionPane.message(new FlowPanel(lb, pf)).show(title = "Preferences")
  }

  class Document {
    var file  = Option.empty[File]
    val peer  = new PlainDocument()
    def name: String = file.fold("Untitled")(_.base)
  }

  private class DocumentWindow(document: Document) extends WindowImpl {
    win =>

    def handler = TextEdit.windowHandler
    title = document.name
    size  = (400, 200)
    file  = document.file
    component.background = Color.white

    contents = new ScrollPane(new TextArea(12, 60) {
      peer.setDocument(win.document.peer)
    })

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
      "view.find-window" -> new Action(null) {
        def apply(): Unit = {
          val winOpt = Window.find(contents.head)
          OptionPane.message(s"Found: ${winOpt.map(_.title)}").show(winOpt)
        }
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
    file.foreach { f =>
      try {
        val source = io.Source.fromFile(f, "UTF-8")
        try {
          val text = source.getLines().mkString("\n")
          doc.peer.insertString(0, text, null)
        } finally {
          source.close()
        }
      } catch {
        case ex: IOException =>
          Window.showDialog(ex -> s"Open ${f.name}")
      }
    }
    documentHandler.addDocument(doc)
    val w     = new DocumentWindow(doc)
    docs     += doc -> w
  }
}