/*
 *  DialogSource.scala
 *  (Desktop)
 *
 *  Copyright (c) 2013 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU General Public License
 *	as published by the Free Software Foundation; either
 *	version 2, june 1991 of the License, or (at your option) any later version.
 *
 *	This software is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *	General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public
 *	License (gpl.txt) along with this software; if not, write to the Free Software
 *	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

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