/*
 *  DialogSource.scala
 *  (Desktop)
 *
 *  Copyright (c) 2013-2014 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU Lesser General Public License v3+
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss
package desktop

import java.util.StringTokenizer


object DialogSource {
  implicit final class Dialog(val source: swing.Dialog) extends DialogSource[Unit] {
    def show(window: Option[Window]): Unit = source.open()
  }

  implicit final class Exception(val source: (scala.Throwable, String)) extends DialogSource[Unit] {
    private def uncamelizeClassName(x: Any): String = {
      val cn0 = x.getClass.getName
      val i   = cn0.lastIndexOf('.')
      val cn  = cn0.substring(i + 1)
      val len = cn.length
      val b   = new StringBuilder(len + len/2)
      var j   = 0
      var wasUpper = true
      while (j < len) {
        val c       = cn.charAt(j)
        val isUpper = c.isUpper
        if (!wasUpper && isUpper) b.append(' ')
        b.append(c)
        wasUpper    = isUpper
        j += 1
      }
      b.result()
    }

    def show(window: Option[Window]): Unit = {
      val (exception, title) = source
      val name    = if (exception == null) "Exception" else uncamelizeClassName(exception)
      val strBuf  = new StringBuilder(name)
      val message = if (exception == null) "null" else {
        val loc = exception.getLocalizedMessage
        if (loc == null) {
          val m = exception.getMessage
          if (m == null) "null" else m
        } else loc
      }
      var lineLen = 0
      val options = Seq("Ok", "Show Stack Trace")
      val tok     = new StringTokenizer(message)
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
      val op = desktop.OptionPane(message = strBuf.toString(), messageType = desktop.OptionPane.Message.Error,
        optionType = desktop.OptionPane.Options.YesNo, entries = options, initial = Some(options(0)))
      op.title = title

      if (op.show(window).id == 1) {
        exception.printStackTrace()
      }
    }
  }
}
trait DialogSource[+A] {
  def show(window: Option[Window]): A
}