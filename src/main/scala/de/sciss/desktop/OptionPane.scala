/*
 *  OptionPane.scala
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

import javax.{swing => j}
import scala.swing.{UIElement, Swing, Component, Dialog}
import javax.swing.{JOptionPane, Icon}
import scala.swing.Swing.EmptyIcon
import collection.breakOut

object OptionPane {
  val Message = Dialog.Message
  val Options = Dialog.Options
  val Result  = Dialog.Result

  def message(message: Any, messageType: Message.Value = Message.Info, icon: Icon = EmptyIcon,
              focus: Option[Component] = None): OptionPane[Unit] =
    new Impl[Unit] {
      lazy val peer = new JOption(message, messageType, Options.Default, icon, Nil, None, focus)
      def result {}
    }

  def confirmation(message: Any, optionType: Options.Value = Options.YesNo,
                   messageType: Message.Value = Message.Question, icon: Icon = EmptyIcon,
                   focus: Option[Component] = None): OptionPane[Result.Value] =
    new Impl[Result.Value] {
      lazy val peer = new JOption(message, messageType, optionType, icon, Nil, None, focus)
      def result: Result.Value = {
        val j = peer.getValue match {
          case i: Integer => i.intValue()
          case _ => JOptionPane.CLOSED_OPTION
        }
        Result(j)
      }
    }

  def apply(message: Any, optionType: Options.Value = Options.YesNo, messageType: Message.Value = Message.Question,
            icon: Icon = EmptyIcon, entries: Seq[Any] = Nil, initial: Option[Any] = None,
            focus: Option[Component] = None): OptionPane[Result.Value] =
    new Impl[Result.Value] {
      lazy val peer = new JOption(message, messageType, optionType, icon, entries, initial, focus)
      def result: Result.Value = {
        val i = peer.getValue
        val j = if (i == null) JOptionPane.CLOSED_OPTION else {
          if (entries.nonEmpty) {
            val k = entries.indexOf(i)
            if (k < 0) JOptionPane.CLOSED_OPTION else k
          } else {
            i match {
              case ii: Integer => ii.intValue()
              case _ => JOptionPane.CLOSED_OPTION
            }
          }
        }
        Result(j)
      }
    }

  private abstract class Impl[A] extends OptionPane[A] {
    override def toString = s"OptionPane@${hashCode().toHexString}"
  }

  private def wrapMessage(message: Any): Any = message match {
    case ui: UIElement => ui.peer
    case other => other
  }

  private final class JOption(message: Any, messageType: Message.Value, optionType: Options.Value, icon: Icon,
                              options: Seq[Any], initialValue: Option[Any], focus: Option[Component])
    extends JOptionPane(wrapMessage(message), messageType.id, optionType.id, Swing.wrapIcon(icon),
      if (options.isEmpty) null else (options.map(wrapMessage(_).asInstanceOf[AnyRef])(breakOut): Array[AnyRef]),
      initialValue.map(wrapMessage).orNull) {

    override def selectInitialValue() {
      focus match {
        case Some(c) => c.requestFocusInWindow()
        case _ => super.selectInitialValue()
      }
    }
  }
}
sealed trait OptionPane[A] {
  def peer: j.JOptionPane
  def result: A
//  def createDialog(parent: UIElement, title: String): Dialog
}