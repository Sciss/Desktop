package de.sciss.desktop

import de.sciss.desktop.impl.{SwingApplicationImpl, WindowImpl, WindowHandlerImpl}
import de.sciss.desktop.Menu._
import de.sciss.desktop.KeyStrokes._
import java.awt.event.KeyEvent._
import scala.swing.Swing._

object WindowHandlerTest extends SwingApplicationImpl("Window Handler Test") {
  type Document = Unit

  override lazy val windowHandler: WindowHandler = new WindowHandlerImpl(this, menuFactory) {
    override def usesInternalFrames: Boolean = false
  }

  lazy val menuFactory: Root = Root().add(Group("file", "File").add(Item("new")("New" -> (menu1 + VK_N)) {
    new WindowImpl {
      def handler = windowHandler
      override def style: Window.Style = Window.Auxiliary
      title     = "Foo Bar"
      size      = (300, 300)
      location  = (150, 150)
      front()
    }
  }))

  override def init(): Unit = new WindowImpl {
    def handler = windowHandler
    title = "Main Window"
    size = (300, 300)
    front()
  }
}
