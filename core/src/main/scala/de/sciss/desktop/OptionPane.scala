/*
 *  OptionPane.scala
 *  (Desktop)
 *
 *  Copyright (c) 2013-2021 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.desktop

import javax.swing.{Icon, JOptionPane}
import javax.{swing => j}

import scala.swing.{Component, Dialog, Swing, UIElement}
import scala.swing.Swing.EmptyIcon

object OptionPane {
  val Message = Dialog.Message
  val Options = Dialog.Options
  val Result  = Dialog.Result

  private final case class User(id: Int) extends Result.Value

  def message(message: Any, messageType: Message.Value = Message.Info, icon: Icon = EmptyIcon,
              focus: Option[Component] = None): OptionPane[Unit] =
    new Impl[Unit] {
      protected lazy val _messageType: Message.Value = messageType

      lazy val peer = new JOption(message, messageType, Options.Default, icon, Nil, None, focus)

      def result: Unit = ()
    }

  def confirmation(message: Any, optionType: Options.Value = Options.YesNo,
                   messageType: Message.Value = Message.Question, icon: Icon = EmptyIcon,
                   focus: Option[Component] = None): OptionPane[Result.Value] =
    new Impl[Result.Value] {
      protected lazy val _messageType: Message.Value = messageType

      lazy val peer = new JOption(message, messageType, optionType, icon, Nil, None, focus)

      def result: Result.Value = {
        val j = peer.getValue match {
          case i: Integer => i.intValue()
          case _ => JOptionPane.CLOSED_OPTION
        }
        Result(j)
      }
    }

  def textInput(message: Any, messageType: Message.Value = Message.Question, icon: Icon = EmptyIcon,
               initial: String): OptionPane[Option[String]] =
    new Impl[Option[String]] {
      protected lazy val _messageType: Message.Value = messageType

      lazy val peer = new JOption(message, messageType, Options.OkCancel, icon, Nil, None, None)

      peer.setWantsInput(true)
      peer.setInitialSelectionValue(initial)

      def result: Option[String] = peer.getInputValue match {
        case JOptionPane.UNINITIALIZED_VALUE => None
        case value => Some(value.toString)
      }
    }

  def comboInput[A](message: Any, messageType: Message.Value = Message.Question, icon: Icon = EmptyIcon,
               options: Seq[A], initial: A): OptionPane[Option[A]] =
    new Impl[Option[A]] {
      protected lazy val _messageType: Message.Value = messageType

      lazy val peer = new JOption(message, messageType, Options.OkCancel, icon, Nil, None, None)

      peer.setWantsInput(true)
      // array must not be null, otherwise option pane uses text field
      peer.setSelectionValues(if (options.isEmpty) new Array[AnyRef](0) else optionsToJava(options))
      peer.setInitialSelectionValue(initial)

      def result: Option[A] = peer.getInputValue match {
        case JOptionPane.UNINITIALIZED_VALUE => None
        case value => Option(value.asInstanceOf[A]) // with empty options, value might be null
      }
    }

  def apply(message: Any, optionType: Options.Value = Options.YesNo, messageType: Message.Value = Message.Question,
            icon: Icon = EmptyIcon, entries: Seq[Any] = Nil, initial: Option[Any] = None,
            focus: Option[Component] = None): OptionPane[Result.Value] =
    new Impl[Result.Value] {
      protected lazy val _messageType: Message.Value = messageType

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
        User(j)
      }
    }

  private abstract class Impl[A] extends OptionPane[A] {
    override def toString = s"OptionPane@${hashCode().toHexString}"

    protected def _messageType: Message.Value

    var title: String = _messageType match {
      case Message.Plain    => "Message"
      case Message.Info     => "Notice"
      case Message.Question => "Request"
      case Message.Warning  => "Warning"
      case Message.Error    => "Error"
    }

    var resizable = false
  }

  private def wrapMessage(message: Any): Any = message match {
    case ui: UIElement => ui.peer
    case other => other
  }

  private def optionsToJava(options: Seq[Any]): Array[AnyRef] =
    if (options.isEmpty) null else options.iterator.map(wrapMessage(_).asInstanceOf[AnyRef]).toArray

  private final class JOption(message: Any, messageType: Message.Value, optionType: Options.Value, icon: Icon,
                              options: Seq[Any], initialValue: Option[Any], focus: Option[Component])
    extends JOptionPane(wrapMessage(message), messageType.id, optionType.id, Swing.wrapIcon(icon),
      optionsToJava(options), initialValue.map(wrapMessage).orNull) {

    override def selectInitialValue(): Unit =
      focus match {
        case Some(c) => c.requestFocusInWindow()
        case _ => super.selectInitialValue()
      }
  }
}
sealed trait OptionPane[A] extends DialogSource[A] {
  /** The underlying `javax.swing` peer. */
  def peer      : j.JOptionPane
  def result    : A
  var title     : String
  var resizable : Boolean

  def show(window: Option[Window]): A = show(window, title = title)

  def show(window: Option[Window] = None, title: String = this.title): A = {
    showImpl(window, title = title, modal = true)
    result
  }

  def showNonModal(window: Option[Window]): Unit = showNonModal(window, title = title)

  def showNonModal(window: Option[Window] = None, title: String = this.title): Unit = {
    showImpl(window, title = title, modal = false)
    result
  }

  private def showImpl(window: Option[Window], title: String, modal: Boolean): Unit = {
    val parent  = window.map(w => Window.peer(w)).orNull
    val jDlg    = peer.createDialog(parent, title)
    if (!modal   ) jDlg.setModal    (false)
    if (resizable) jDlg.setResizable(true )
    jDlg.setVisible(true)
    window.foreach(_.front())
  }
}