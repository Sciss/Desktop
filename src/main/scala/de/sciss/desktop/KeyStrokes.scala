package de.sciss.desktop

import java.awt.event.InputEvent
import javax.swing.KeyStroke

object KeyStrokes {
  import InputEvent._

  object Modifiers {
//    def unapply(modifiers: Modifiers): Option[Int] = Some(modifiers.mask)
    def apply(mask: Int): Modifiers = new Impl(mask)

    final case class Impl(mask: Int) extends Modifiers {
      override def productPrefix = "Modifiers"
    }
  }
  trait Modifiers {
    def mask: Int
    final def +(that: Modifiers)  = Modifiers(mask + that.mask)
    final def +(code: Int)        = KeyStroke.getKeyStroke(code, mask)
    final def +(char: Char)       = KeyStroke.getKeyStroke(char, mask)
  }
  case object shift extends Modifiers { final val mask = SHIFT_MASK }
  case object alt   extends Modifiers { final val mask = ALT_MASK   }
  case object ctrl  extends Modifiers { final val mask = CTRL_MASK  }
  case object meta  extends Modifiers { final val mask = META_MASK  }
  case object plain extends Modifiers {
    final val mask = 0
    def apply(code: Int)  = KeyStroke.getKeyStroke(code, 0)
    def apply(char: Char) = KeyStroke.getKeyStroke(char)
  }
  case object menu1 extends Modifiers { val mask = Window.menuShortcut }
  case object menu2 extends Modifiers { val mask = if (menu1.mask == CTRL_MASK) CTRL_MASK | ALT_MASK else CTRL_MASK }
}