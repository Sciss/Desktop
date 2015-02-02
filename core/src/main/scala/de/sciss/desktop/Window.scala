/*
 *  Window.scala
 *  (Desktop)
 *
 *  Copyright (c) 2013-2015 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.desktop

import javax.swing.{JFrame, RootPaneContainer, SwingUtilities, WindowConstants}
import java.awt.event.WindowEvent
import javax.swing.event.InternalFrameEvent
import de.sciss.desktop.impl.WindowImpl

import annotation.switch
import java.awt
import awt.{GraphicsEnvironment, Toolkit, Point, Dimension, Rectangle}
import swing.{Reactions, RootPanel, Action, UIElement}

object Window {
  sealed trait Style
  /** Regular full-fledged window. */
  case object Regular   extends Style
  /** Supplementary window which for example might not need menu bar. */
  case object Auxiliary extends Style
  /** Supplementary window which is a (possibly floating) palette. */
  case object Palette   extends Style

  private[desktop] def peer(w: Window): awt.Frame = w.component match {
    case j: JFrame => j
    case _ => w.handler.mainWindow.component match {
      case j: JFrame => j
      case _ => null
    }
  }

  object CloseOperation {
    def apply(id: Int): CloseOperation = (id: @switch) match {
      case CloseIgnore .id => CloseIgnore
      case CloseExit   .id => CloseExit
      case CloseHide   .id => CloseHide
      case CloseDispose.id => CloseDispose
    }
  }
  sealed trait CloseOperation { def id: Int }
  case object CloseIgnore  extends CloseOperation { final val id = WindowConstants.DO_NOTHING_ON_CLOSE  }
  case object CloseExit    extends CloseOperation { final val id = WindowConstants.EXIT_ON_CLOSE        }
  case object CloseHide    extends CloseOperation { final val id = WindowConstants.HIDE_ON_CLOSE        }
  case object CloseDispose extends CloseOperation { final val id = WindowConstants.DISPOSE_ON_CLOSE     }

  object Event {
    def apply(window: Window, peer: WindowEvent): Event = {
      import WindowEvent._
      (peer.getID: @switch) match {
        case WINDOW_ACTIVATED   => Activated  (window)
        case WINDOW_CLOSED      => Closed     (window)
        case WINDOW_CLOSING     => Closing    (window)
        case WINDOW_DEACTIVATED => Deactivated(window)
        case WINDOW_DEICONIFIED => Deiconified(window)
        case WINDOW_ICONIFIED   => Iconified  (window)
        case WINDOW_OPENED      => Opened     (window)
      }
    }
    def apply(window: Window, peer: InternalFrameEvent): Event = {
      import InternalFrameEvent._
      (peer.getID: @switch) match {
        case INTERNAL_FRAME_ACTIVATED   => Activated  (window)
        case INTERNAL_FRAME_CLOSED      => Closed     (window)
        case INTERNAL_FRAME_CLOSING     => Closing    (window)
        case INTERNAL_FRAME_DEACTIVATED => Deactivated(window)
        case INTERNAL_FRAME_DEICONIFIED => Deiconified(window)
        case INTERNAL_FRAME_ICONIFIED   => Iconified  (window)
        case INTERNAL_FRAME_OPENED      => Opened     (window)
      }
    }
  }
  sealed trait Event extends swing.event.Event {
    def source: Window
  }

  final case class Activated  (source: Window) extends Event
  final case class Closed     (source: Window) extends Event
  final case class Closing    (source: Window) extends Event
  final case class Deactivated(source: Window) extends Event
  final case class Deiconified(source: Window) extends Event
  final case class Iconified  (source: Window) extends Event
  final case class Opened     (source: Window) extends Event

  def find(component: UIElement): Option[Window] = {
    val rp = SwingUtilities.getAncestorOfClass(classOf[RootPaneContainer], component.peer)
    if (rp == null) return None
    val w = rp.asInstanceOf[RootPaneContainer].getRootPane.getClientProperty(WindowImpl.Property)
    if (w == null) return None
    Some(w.asInstanceOf[Window])
  }

  def showDialog[A](parent: UIElement, source: DialogSource[A]): A = {
    find(parent) match {
      case some @ Some(w) => w.handler.showDialog(some, source)
      case _              => showDialog(source)
    }
  }

  def showDialog[A](source: DialogSource[A]): A = {
    source.show(None)
  }

  def menuShortcut: Int = Toolkit.getDefaultToolkit.getMenuShortcutKeyMask

  def availableSpace: Rectangle = GraphicsEnvironment.getLocalGraphicsEnvironment.getMaximumWindowBounds

  object Actions {
    def show(window: Window): Action = new ShowAction(window)
  }

  private final class ShowAction(window: Window) extends Action(window.title) {
    window.reactions += {
      case Window.Activated(_) =>
//				if( !disposed ) {
          // ((BasicApplication) AbstractApplication.getApplication()).getMenuFactory().setSelectedWindow( ShowWindowAction.this );
          // XXX TODO
//			  }
    }

	  def apply(): Unit = {
      window.visible = true
      window.front()
    }

//    def dispose(): Unit =
//      w.reactions -= ...
	}
}
/** Interface that unites functionality
  *	from inhomogeneous classes such as JFrame, JDialog, JInternalFrame
  */
trait Window {
  def handler: WindowHandler

  def title: String
  var visible: Boolean

  def component: RootPanel

  def dispose(): Unit
  def front(): Unit

  def floating: Boolean
  def active: Boolean

  def resizable: Boolean

  var alwaysOnTop: Boolean

  def size: Dimension
  def bounds: Rectangle

  var location: Point
  def reactions: Reactions
}