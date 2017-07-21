/*
 *  DialogSource.scala
 *  (Desktop)
 *
 *  Copyright (c) 2013-2017 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss
package desktop

import de.sciss.desktop.OptionPane.Result

object DialogSource {
  implicit final class Dialog(val source: swing.Dialog) extends DialogSource[Unit] {
    def show(window: Option[Window]): Unit = source.open()
  }

  def exceptionToOptionPane(exception: scala.Throwable): OptionPane[Result.Value] = {
    val message   = Util.formatException(exception, margin = 40, stackTraceLines = 0)
    val optionOk  = "Ok"
    val options   = Seq(optionOk, "Show Stack Trace")
    val op        = desktop.OptionPane(message = message, messageType = desktop.OptionPane.Message.Error,
      optionType = desktop.OptionPane.Options.YesNo, entries = options, initial = Some(optionOk))
    op
  }

  implicit final class Exception(val source: (scala.Throwable, String)) extends DialogSource[Unit] {
    def show(window: Option[Window]): Unit = {
      val (exception, title) = source
      val op = exceptionToOptionPane(exception)
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