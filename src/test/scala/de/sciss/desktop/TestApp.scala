package de.sciss.desktop

import impl.{MainWindowImpl, WindowHandlerImpl, DocumentHandlerImpl, ApplicationImpl}
import swing.Swing
import Swing._
import javax.swing.KeyStroke
import java.awt
import awt.FileDialog
import awt.event.KeyEvent
import java.io.File

object TestApp extends ApplicationImpl with SwingApplication with App {
  Swing.onEDT {
    new MainWindow
  }

  type Document = Unit

  def quit() {
    sys.exit()
  }

  def name = "Test"

  lazy val documentHandler = new DocumentHandlerImpl[Unit]

  private lazy val menu = {
    import Menu._
    Root()
      .add(Group("file", "File")
        .add(Item("open")("Open..." -> KeyStroke.getKeyStroke(KeyEvent.VK_O, Window.menuShortcut)) {
          val dlg = new FileDialog(null: awt.Frame, "Open File")
          dlg.setVisible(true)
          val f = if (dlg.getFile == null) None else Some(new File(dlg.getDirectory, dlg.getFile))
          println(s"Result: $f")
        })
      )
  }

  lazy val windowHandler = new WindowHandlerImpl(this, menu)

  private class MainWindow extends MainWindowImpl {
    def handler = TestApp.windowHandler
    title = "Main"
    size  = (400, 200)
    front()
  }
}