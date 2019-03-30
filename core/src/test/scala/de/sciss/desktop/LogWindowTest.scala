package de.sciss.desktop

import de.sciss.desktop.Menu.Root
import de.sciss.desktop.impl.{LogWindowImpl, SwingApplicationImpl, WindowImpl}
import de.sciss.submin.Submin

import scala.swing.{Button, FlowPanel, Rectangle}

object LogWindowTest extends SwingApplicationImpl("Log Test") {
  protected lazy val menuFactory: Root = Root()

  type Document = Unit

  override protected def init(): Unit = {
    val isDark = args.contains("--dark")
    Submin.install(isDark)

    new LogWindowImpl {
      def handler: WindowHandler = LogWindowTest.windowHandler

      bounds = new Rectangle(400, 100, 400, 400)
    }

    new WindowImpl {
      def handler: WindowHandler = LogWindowTest.windowHandler

      val but1: Button = Button("Print Something") {
        println("Yes.")
      }

      val but2: Button = Button("Who?")(println(System.out))

      contents = new FlowPanel(but1, but2)

      closeOperation = Window.CloseExit

      pack()
      front()
    }
  }
}
