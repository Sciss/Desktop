package de.sciss.desktop

import com.alee.laf.WebLookAndFeel

import scala.swing._
import javax.swing.WindowConstants
import language.reflectiveCalls
import scala.Some

object DialogFocusTest extends App {
  Swing.onEDT {
    WebLookAndFeel.install()

    lazy val button: Button = Button("Test") { val res = scalaTest(); button.text = res.toString }
    new Frame {
      title = "Dialog Test"
      contents = button
      peer.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
      pack()
      centerOnScreen()
      open()
    }
  }

  private def scalaTest(): OptionPane.Result.Value = {
    val lbKey = new Label("Key:")
    val lbVal = new Label("Value:")
    val ggKey = new TextField(16)
    val ggVal = new TextField(16)

    lbKey.horizontalAlignment = Alignment.Trailing
    lbVal.horizontalAlignment = Alignment.Trailing

    val box = new GridBagPanel {
      val cons = new Constraints()
      import cons.{gridx, gridy}
      gridx = 0; gridy = 0
      layout(lbKey) = cons
      gridx += 1
      layout(ggKey) = cons
      gridx = 0
      gridy += 1
      layout(lbVal) = cons
      gridx += 1
      layout(ggVal) = cons
    }

    val pane  = OptionPane.confirmation(message = box, messageType = Dialog.Message.Question,
      optionType = Dialog.Options.OkCancel, focus = Some(ggVal))
    val dlg   = pane.peer.createDialog(null, "New Entry")
    dlg.setVisible(true)
    pane.result
  }
}