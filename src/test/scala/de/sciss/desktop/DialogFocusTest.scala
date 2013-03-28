package de.sciss.desktop

import swing.{Frame, Button, Dialog, BoxPanel, Orientation, Alignment, TextField, Label, Swing}
import scalaswingcontrib.group.GroupPanel
import javax.swing.{WindowConstants, JOptionPane, GroupLayout, JPanel, SwingConstants, JTextField, JLabel}
import javax.swing.event.{AncestorEvent, AncestorListener}

object DialogFocusTest extends App {
  Swing.onEDT {
//    scalaTest()
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

  private def javaTest() {
    val lbKey = new JLabel("Key:")
    val lbVal = new JLabel("Value:")
    val ggKey = new JTextField(16)
    val ggVal = new JTextField(16)

    lbKey.setHorizontalAlignment(SwingConstants.TRAILING)
    lbVal.setHorizontalAlignment(SwingConstants.TRAILING)

    val box = new JPanel
    val lay = new GroupLayout(box)
    box.setLayout(lay)
    lay.setAutoCreateGaps(true)
    lay.setAutoCreateContainerGaps(true)

    val hGroup  = lay.createSequentialGroup()
    hGroup.addGroup(lay.createParallelGroup().addComponent(lbKey).addComponent(lbVal))
    hGroup.addGroup(lay.createParallelGroup().addComponent(ggKey).addComponent(ggVal))
    lay.setHorizontalGroup(hGroup)

    val vGroup  = lay.createSequentialGroup()
    vGroup.addGroup(lay.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(lbKey).addComponent(ggKey))
    vGroup.addGroup(lay.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(lbVal).addComponent(ggVal))
    lay.setVerticalGroup(vGroup)

//    ggVal.addAncestorListener(new AncestorListener() {
//      def ancestorAdded(e: AncestorEvent) {
//        val c = ggVal // e.getComponent
//        c.requestFocusInWindow()
//        c.removeAncestorListener(this)
//      }
//
//      def ancestorRemoved(e: AncestorEvent) {}
//      def ancestorMoved(e: AncestorEvent) {}
//    })

//    JOptionPane.showConfirmDialog(null, box, "New Entry", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)
//    JOptionPane.showOptionDialog(null, box, "New Entry", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null)

    val op  = new JOptionPane(box, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION) {
      override def selectInitialValue() {
        ggVal.requestFocusInWindow()
      }
    }
    val dlg = op.createDialog(null, "New Entry")
    dlg.setVisible(true)
  }
}