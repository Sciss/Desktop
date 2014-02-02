/*
 *  Implicits.scala
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

package de.sciss.desktop

import scala.swing.{Component, Action}
import javax.{swing => j}
import j.KeyStroke

object Implicits {
  //  implicit object ParamPrefs extends Preferences.Type[Param] {
  //    private[desktop] def toString(value: Param): String = value.toString
  //
  //    private[desktop] def valueOf(string: String): Option[Param] = try {
  //      Some(Param.valueOf(string))
  //    } catch {
  //      case NonFatal(_) => None
  //    }
  //  }

  //  private final class DialogFocusListener extends AncestorListener() {
  //    def ancestorAdded(e: AncestorEvent ) {
  //      val c = e.getComponent
  ////      println(s"ANCESTOR ADDED $c")
  ////      Swing.onEDT(
  //      val t = new javax.swing.Timer(100, Swing.ActionListener(_ => c.requestFocusInWindow()))
  //      t.setRepeats(false)
  //      t.start()
  ////      c.requestFocus()
  ////      )
  //      c.removeAncestorListener(this)
  //    }
  //
  //    def ancestorRemoved(e: AncestorEvent) {}
  //    def ancestorMoved  (e: AncestorEvent) {}
  //  }

  implicit final class DesktopComponent(val component: Component) extends AnyVal {
    def addAction(key: String, action: Action, focus: FocusType = FocusType.Default): Unit = {
      val a       = action.peer
      //      val key     = a.getValue(j.Action.NAME).toString
      val stroke  = action.accelerator.getOrElse(throw new IllegalArgumentException(s"addAction($key, $action) - no accelerator found"))
      component.peer.registerKeyboardAction(a, key, stroke, focus.id)
    }

    def removeAction(action: Action): Unit = {
      val a       = action.peer
      val stroke  = a.getValue(j.Action.ACCELERATOR_KEY).asInstanceOf[KeyStroke]
      component.peer.unregisterKeyboardAction(stroke)
    }

    //    def initialDialogFocus(): A = {
    ////      println(s"INITIAL FOCUS ${component.peer}")
    //      component.peer.addAncestorListener(new DialogFocusListener)
    //      component
    //    }
  }

  //  implicit final class DesktopFile(val file: File) extends AnyVal {
  //    def / ()
  //  }
}