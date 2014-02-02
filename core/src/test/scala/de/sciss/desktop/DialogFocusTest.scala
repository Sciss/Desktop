package de.sciss.desktop

import swing.{Frame, Button, Dialog, Alignment, TextField, Label, Swing}
import scalaswingcontrib.group.GroupPanel
import javax.swing.WindowConstants
import language.reflectiveCalls

object DialogFocusTest extends App {
  Swing.onEDT {
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

    val box = new GroupPanel {
      theHorizontalLayout is Sequential(
        Parallel(lbKey, lbVal), Parallel(ggKey, ggVal)
      )
      theVerticalLayout is Sequential(
        Parallel(Baseline)(lbKey, ggKey), Parallel(Baseline)(lbVal, ggVal)
      )
    }

    val pane  = OptionPane.confirmation(message = box, messageType = Dialog.Message.Question,
      optionType = Dialog.Options.OkCancel, focus = Some(ggVal))
    val dlg   = pane.peer.createDialog(null, "New Entry")
    dlg.setVisible(true)
    pane.result
  }
}