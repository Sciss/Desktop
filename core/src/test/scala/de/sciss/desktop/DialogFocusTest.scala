package de.sciss.desktop

import de.sciss.desktop.impl.{SwingApplicationImpl, WindowImpl}
import de.sciss.submin.Submin

import scala.language.reflectiveCalls
import scala.swing.event.Key
import scala.swing.{Action, Alignment, Button, Dialog, FlowPanel, GridBagPanel, Label, TextField}

object DialogFocusTest extends SwingApplicationImpl("Dialog Focus Test") {
  type Document = Unit

  override protected def init(): Unit = {
    val isDark = args.contains("--dark")
    Submin.install(isDark)
    import Implicits._
    label.addAction("de.sciss.desktop.Foo", action, FocusType.Window)
    frame.front()
  }

  lazy val button: Button = Button("Test") {
    val res = test()
    button.text = res.toString
  }
  lazy val label = new Label("Label reacts to Control-L")

  lazy val action: Action = new Action("Foo") {
    accelerator = Some(KeyStrokes.ctrl + Key.L)

    def apply(): Unit = label.text = s"${label.text}!"
  }

  lazy val frame: Window = new WindowImpl {
    title           = "Dialog Test"
    contents        = new FlowPanel(button, label)
    closeOperation  = Window.CloseExit
    pack()
    Util.centerOnScreen(this)

    def handler: WindowHandler = windowHandler
  }
  
  protected def menuFactory: Menu.Root = Menu.Root()

  def test(): OptionPane.Result.Value = {
    val lbKey = new Label("Key:")
    val lbVal = new Label("Value:")
    val ggKey = new TextField(16)
    val ggVal = new TextField(16)

    lbKey.horizontalAlignment = Alignment.Trailing
    lbVal.horizontalAlignment = Alignment.Trailing

    val box: GridBagPanel = new GridBagPanel {
      val cons = new Constraints()
      import cons._
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
    pane.show(Some(frame), "New Entry")
    pane.result
  }
}