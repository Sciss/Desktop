package de.sciss.desktop

import java.awt.Color
import java.io.IOException
import javax.swing.text.PlainDocument

import de.sciss.desktop.impl.{SwingApplicationImpl, WindowHandlerImpl, WindowImpl}
import de.sciss.file._
import de.sciss.submin.Submin

import scala.swing.Swing._
import scala.swing.{Action, FlowPanel, Label, ScrollPane, TextArea}

class TextEditDocument {
  var file  = Option.empty[File]
  val peer  = new PlainDocument()
  def name: String = file.fold("Untitled")(_.base)
}

object TextEdit extends SwingApplicationImpl[TextEditDocument]("TextEdit") { app =>
  type Document = TextEditDocument

  val USE_WEBLAF = true

  override protected def init(): Unit = if (USE_WEBLAF) {
    val isDark = args.contains("--dark")
    Submin.install(isDark)
  }

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

  protected lazy val miDarkBackground: Menu.CheckBox = {
    import de.sciss.desktop.Menu._
    CheckBox("darkBg", proxy("Dark Background"))
  }

  protected lazy val menuFactory: Menu.Root = {
    import de.sciss.desktop.KeyStrokes._
    import de.sciss.desktop.Menu._

    import scala.swing.event.Key

    val root = Root()
      .add(Group("file", "File")
        .add(Item("new")("New" -> (menu1 + Key.N))(newDocument()))
        .add(Item("open")("Open..." -> (menu1 + Key.O)) {
          println(baseDirectoryPrefs.get)
          val dlg = FileDialog.open(init = baseDirectoryPrefs.get)
          dlg.multiple = true
          val ok  = dlg.show(None).isDefined
          if (ok) dlg.files.foreach(openDocument)
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

  private class DocumentWindow(document: Document) extends WindowImpl {
    win =>

    def handler: WindowHandler = TextEdit.windowHandler

    title = document.name
    size  = (400, 200)
    file  = document.file
    component.background = Color.white

    contents = new ScrollPane(new TextArea(12, 60) {
      peer.setDocument(win.document.peer)
    }) {
      peer.putClientProperty("styleId", "undecorated")
    }

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

  def newDocument(file: Option[File] = None): Unit =
    try {
      val text = file.fold("") { f =>
        val source = io.Source.fromFile(f, "UTF-8")
        try {
          source.getLines().mkString("\n")
        } finally {
          source.close()
        }
      }
      val doc   = new Document
      if (text.nonEmpty) doc.peer.insertString(0, text, null)
      documentHandler.addDocument(doc)
      val w     = new DocumentWindow(doc)
      docs     += doc -> w
    } catch {
      case ex: IOException =>
        Window.showDialog(ex -> s"Open ${file.map(_.name).orNull}")
    }
}