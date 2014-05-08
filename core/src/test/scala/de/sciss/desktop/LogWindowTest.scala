package de.sciss.desktop

import de.sciss.desktop.impl.{WindowImpl, LogWindowImpl, SwingApplicationImpl}
import de.sciss.desktop.Menu.Root
import scala.swing.{FlowPanel, Rectangle, Button}

object LogWindowTest extends SwingApplicationImpl("Log Test") {
  protected lazy val menuFactory: Root = Root()

  type Document = Unit

  override protected def init(): Unit = {
    new LogWindowImpl {
      def handler: WindowHandler = LogWindowTest.windowHandler

      bounds = new Rectangle(400, 100, 400, 400)
    }

    new WindowImpl {
      def handler: WindowHandler = LogWindowTest.windowHandler

      val but1 = Button("Print Something") {
        println("Yes.")
      }

      val but2 = Button("Who?")(println(System.out))

      contents = new FlowPanel(but1, but2)

      closeOperation = Window.CloseExit

      pack()
      front()
    }
  }
}
