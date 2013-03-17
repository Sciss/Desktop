package de.sciss.desktop

import swing.{Action, Reactions, Component}
import javax.swing.{RootPaneContainer, SwingUtilities, WindowConstants}
import java.awt._
import java.awt.event.WindowEvent
import javax.swing.event.InternalFrameEvent
import annotation.switch
import scala.Some

object Window {
  sealed trait Style
  /** Regular full-fledged window. */
  case object Regular   extends Style
  /** Supplementary window which for example might not need menu bar. */
  case object Auxiliary extends Style
  /** Supplementary window which is a (possibly floating) palette. */
  case object Palette   extends Style

  sealed trait CloseOperation { def id: Int }
  case object CloseIgnore  extends CloseOperation { val id = WindowConstants.DO_NOTHING_ON_CLOSE  }
  case object CloseExit    extends CloseOperation { val id = WindowConstants.EXIT_ON_CLOSE        }
  case object CloseHide    extends CloseOperation { val id = WindowConstants.HIDE_ON_CLOSE        }
  case object CloseDispose extends CloseOperation { val id = WindowConstants.DISPOSE_ON_CLOSE     }

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

  def find(component: Component): Option[Window] = {
    val rp = SwingUtilities.getAncestorOfClass(classOf[RootPaneContainer], component.peer)
    if (rp == null) return None
    val w = rp.asInstanceOf[RootPaneContainer].getRootPane.getClientProperty("de.sciss.desktop.Window")
    if (w == null) return None
    Some(w.asInstanceOf[Window])
  }

  def showDialog[A](parent: Component, source: DialogSource[A]): A = {
    find(parent) match {
      case Some(w)  => w.handler.showDialog(w, source)
      case _        => showDialog(source)
    }
  }

  def showDialog[A](source: DialogSource[A]): A = {
    source.show()
  }

//  def showDialog(parent: Component, pane: JOptionPane, title: String): Any = {
//    findWindow(parent) match {
//      case Some(w)  => w.handler.showDialog(w, pane, title)
//      case _        => showDialog(pane, title)
//    }
//  }
//
//  def showDialog(pane: JOptionPane, title: String): Any = {
//    val jdlg  = pane.createDialog(title)
//    val dlg   = new Dialog(null) {
//      override lazy val peer = jdlg
//    }
//    showDialog(dlg)
//    pane.getValue
//  }
//
//  def showErrorDialog(exception: Exception, title: String) {
//    val strBuf = new StringBuffer("Exception: ")
//    val message = if (exception == null) "null" else (exception.getClass.getName + " - " + exception.getLocalizedMessage)
//    var lineLen = 0
//    val options = Array[AnyRef]("Ok", "Show Stack Trace")
//    val tok = new StringTokenizer(message)
//    strBuf.append(":\n")
//    while (tok.hasMoreTokens) {
//      val word = tok.nextToken()
//      if (lineLen > 0 && lineLen + word.length() > 40) {
//        strBuf.append("\n")
//        lineLen = 0
//      }
//      strBuf.append(word)
//      strBuf.append(' ')
//      lineLen += word.length() + 1
//    }
//    val op = new JOptionPane(strBuf.toString, JOptionPane.ERROR_MESSAGE, JOptionPane.YES_NO_OPTION, null, options, options(0))
//    if (showDialog(op, title) == 1) {
//      exception.printStackTrace()
//    }
//  }

  def menuShortcut: Int = Toolkit.getDefaultToolkit.getMenuShortcutKeyMask

  def availableSpace: Rectangle = GraphicsEnvironment.getLocalGraphicsEnvironment.getMaximumWindowBounds

  def showAction(window: Window): Action = new ShowAction(window)

  private final class ShowAction(window: Window) extends Action(window.title) {
    window.reactions += {
      case Window.Activated(_) =>
//				if( !disposed ) {
          // ((BasicApplication) AbstractApplication.getApplication()).getMenuFactory().setSelectedWindow( ShowWindowAction.this );
          ???
//			  }
    }

	  def apply() {
      window.visible = true
      window.front()
    }

//    def dispose() {
//      w.reactions -= ...
//    }
	}
}
/** Interface that unites functionality
  *	from inhomogeneous classes such as JFrame, JDialog, JInternalFrame
  */
trait Window {
  def handler: WindowHandler

  def title: String
  var visible: Boolean

  def component: Component

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