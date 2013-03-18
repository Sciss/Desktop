package de.sciss.desktop

import javax.swing.JOptionPane
import java.io.File
import java.util.StringTokenizer

object DialogSource {
  implicit final class Dialog(val source: swing.Dialog) extends DialogSource[Unit] {
    def show() {
      source.open()
    }
  }

  implicit final class OptionPane(val source: (JOptionPane, String)) extends DialogSource[Any] {
    def show(): Any = {
      val (pane, title) = source
      val jdlg  = pane.createDialog(title)
      jdlg.setVisible(true)
      pane.getValue
    }
  }

  implicit final class Exception(val source: (scala.Exception, String)) extends DialogSource[Unit] {
    def show() {
      val (exception, title) = source
      val strBuf = new StringBuffer("Exception: ")
      val message = if (exception == null) "null" else (exception.getClass.getName + " - " + exception.getLocalizedMessage)
      var lineLen = 0
      val options = Array[AnyRef]("Ok", "Show Stack Trace")
      val tok = new StringTokenizer(message)
      strBuf.append(":\n")
      while (tok.hasMoreTokens) {
        val word = tok.nextToken()
        if (lineLen > 0 && lineLen + word.length() > 40) {
          strBuf.append("\n")
          lineLen = 0
        }
        strBuf.append(word)
        strBuf.append(' ')
        lineLen += word.length() + 1
      }
      val op = new JOptionPane(strBuf.toString, JOptionPane.ERROR_MESSAGE, JOptionPane.YES_NO_OPTION, null, options, options(0))
      if (Window.showDialog((op, title)) == 1) {
        exception.printStackTrace()
      }
    }
  }

  implicit final class FileDialog(val source: java.awt.FileDialog) extends DialogSource[Option[File]] {
    def show(): Option[File] = {
      source.setVisible(true)
      val dir   = source.getDirectory
      val file  = source.getFile
      if (dir != null && file != null) Some(new File(dir, file)) else None
    }
  }
}
sealed trait DialogSource[+A] {
  def show(): A
}