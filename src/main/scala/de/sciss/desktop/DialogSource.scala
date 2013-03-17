package de.sciss.desktop

import javax.swing.JOptionPane

object DialogSource {
  implicit final class Dialog(val source: swing.Dialog) extends DialogSource[Unit] {
    def show() {
      source.open()
    }
  }

  implicit final class OptionPane(val source: (JOptionPane, String)) extends DialogSource[Any] {
    def show(): Any = {
      ???
    }
  }
}
sealed trait DialogSource[+A] {
  def show(): A
}