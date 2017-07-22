package de.sciss.desktop

import java.io.File
import java.net.URI

import de.sciss.desktop.impl.{SwingApplicationImpl, WindowImpl}

import scala.concurrent.Promise
import scala.swing.event.ButtonClicked
import scala.swing.{Button, FlowPanel, ScrollPane, Swing, TextArea, ToggleButton}

object MacDemo extends SwingApplicationImpl("Mac Test") {
  protected lazy val menuFactory: Menu.Root = {
    import Menu._
    val itAbout = Item.About(this) {
      val opt = OptionPane.message(s"$name is a demo for the Desktop library.")
      opt.show(title = "About")
    }
    val itPrefs = Item.Preferences(this) {
      val opt = OptionPane.textInput("User Name:", initial = "Foo Bar")
      opt.show(title = "Settings")
    }
    val itQuit = Item.Quit(this)
    val r = Root()
    if (itPrefs.visible || itQuit.visible) {
      val gFile = Group("file", "File")
      r.add(gFile)
      if (itPrefs.visible) gFile.add(itPrefs)
      if (itQuit .visible) gFile.add(itQuit )
    }
    if (itAbout.visible) {
      r.add(Group("help", "Help").add(itAbout))
    }
    r
  }

  type Document = Unit

  override protected def init(): Unit = {
    new WindowImpl {
      win =>

      def handler: WindowHandler = MacDemo.windowHandler

      val text = new TextArea(8, 20) {
        editable = false
      }

      title = "Mac Test 2"

      contents = new FlowPanel(
        Button("Show Home") {
          Desktop.revealFile(new File(sys.props("user.home")))
        },
        Button("Write E-Mail") {
          Desktop.composeMail()
        },
        Button("Browse GitHub") {
          Desktop.browseURI(new URI("https://www.github.com"))
        },
        new ToggleButton("Show Badge") {
          listenTo(this)
          reactions += {
            case ButtonClicked(_) =>
              Desktop.setDockBadge(if (selected) Some("Foo") else None)
          }
        },
        new ScrollPane(text)
      )

      Desktop.addListener {
        case x =>
          text.append(s"$x\n")
          text.caret.position = text.peer.getLineStartOffset(text.lineCount -1)
      }

      Desktop.addQuitAcceptor {
        val p = Promise[Unit]()
        Swing.onEDT {
          val pane = OptionPane.confirmation(message = "Really quit?!")
          val res = pane.show(Some(win), title = "Quit Application")
          val ok = res == OptionPane.Result.Yes
          if (ok) p.success(()) else p.failure(new Exception("Aborted"))
        }
        p.future
      }

      closeOperation = Window.CloseIgnore

      reactions += {
        case Window.Closing(_) =>
          import scala.concurrent.ExecutionContext.Implicits.global
          Desktop.mayQuit().foreach(_ => quit())
      }

      pack()
      front()
    }
  }
}
