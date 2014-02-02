package de.sciss.desktop.mac

import de.sciss.desktop.{OptionPane, Window, WindowHandler, Desktop}
import java.io.File
import de.sciss.desktop.impl.{WindowImpl, SwingApplicationImpl}
import de.sciss.desktop.Menu.Root
import scala.swing.{ToggleButton, ScrollPane, TextArea, Button, FlowPanel}
import scala.swing.event.ButtonClicked
import java.net.URI

object MacTest2 extends SwingApplicationImpl("Mac Test") {
  protected lazy val menuFactory: Root = Root()

  type Document = Unit

  override protected def init(): Unit = {
    new WindowImpl {
      win =>

      def handler: WindowHandler = MacTest2.windowHandler

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
        val pane = OptionPane.confirmation(message = "Really quit?!")
        val res  = pane.show(Some(win), title = "Quit Application")
        res == OptionPane.Result.Yes
      }

      closeOperation = Window.CloseIgnore

      reactions += {
        case Window.Closing(_) => if (Desktop.mayQuit()) quit()
      }

      pack()
      front()
    }
  }
}
