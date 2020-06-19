/*
 *  WindowImpl.scala
 *  (Desktop)
 *
 *  Copyright (c) 2013-2020 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.desktop
package impl

import java.awt.event.{WindowEvent, WindowListener}
import java.awt.{Dimension, Point, Rectangle}

import javax.swing.{JFrame, JInternalFrame}
import javax.swing.event.{InternalFrameEvent, InternalFrameListener}
import de.sciss.file.File

import scala.collection.immutable
import scala.swing.{Action, Component, Reactions, RootPanel}

object WindowImpl {
  // we store this client property in the root panel; the value is the desktop.Window
  final val Property = "de.sciss.desktop.Window"

  object Delegate {
    def internalFrame(window: Window, peer: JInternalFrame, hasMenuBar: Boolean): Delegate =
      new InternalFrame(window, peer, hasMenuBar = hasMenuBar)

    def frame(window: Window, peer: JFrame, hasMenuBar: Boolean, screen: Boolean): Delegate =
      new Frame(window, peer, hasMenuBar = hasMenuBar, screen = screen)

    private final class InternalFrame(window: Window, peer: JInternalFrame, hasMenuBar: Boolean)
      extends Delegate with InternalFrameListener {
      delegate =>

      val component: RootPanel = new RootPanel { def peer: JInternalFrame = delegate.peer }
      val reactions: Reactions = new Reactions.Impl

      peer.addInternalFrameListener(this)
      peer.getRootPane.putClientProperty(Property, window)
      if (hasMenuBar) peer.setJMenuBar(window.handler.menuFactory.create(window).peer)

      def closeOperation        : Window.CloseOperation        = Window.CloseOperation(peer.getDefaultCloseOperation)
      def closeOperation_=(value: Window.CloseOperation): Unit = peer.setDefaultCloseOperation(value.id)

      def title                 : String                       = peer.getTitle
      def title_=         (value: String               ): Unit = peer.setTitle(value)

      def resizable             : Boolean                      = peer.isResizable
      def resizable_=     (value: Boolean              ): Unit = peer.setResizable(value)

      def alwaysOnTop = false // XXX TODO
      def alwaysOnTop_=(value: Boolean): Unit = {
        // XXX TODO
        //        peer.setAlwaysOnTop(value)
      }

      def makeUndecorated(): Unit = {
        // XXX TODO
      }

      def active: Boolean = peer.isSelected

      def pack(): Unit = peer.pack()

      def dispose(): Unit = {
        peer.dispose()
        if (hasMenuBar) window.handler.menuFactory.destroy(window)
      }

      def front(): Unit = {
        if (!peer.isVisible) peer.setVisible(true)
        peer.toFront()
      }

      //      def menu_=(value: MenuBar) { peer.setJMenuBar(value.peer) }

      def internalFrameOpened     (e: InternalFrameEvent): Unit = reactions(Window.Opened     (window))
      def internalFrameClosing    (e: InternalFrameEvent): Unit = reactions(Window.Closing    (window))
      def internalFrameClosed     (e: InternalFrameEvent): Unit = reactions(Window.Closed     (window))
      def internalFrameIconified  (e: InternalFrameEvent): Unit = reactions(Window.Iconified  (window))
      def internalFrameDeiconified(e: InternalFrameEvent): Unit = reactions(Window.Deiconified(window))
      def internalFrameActivated  (e: InternalFrameEvent): Unit = reactions(Window.Activated  (window))
      def internalFrameDeactivated(e: InternalFrameEvent): Unit = reactions(Window.Deactivated(window))
    }

    private final class Frame(window: Window, peer: JFrame, hasMenuBar: Boolean, screen: Boolean)
      extends Delegate with WindowListener {
      delegate =>

      val component: RootPanel = new RootPanel { def peer: JFrame = delegate.peer }
      val reactions = new Reactions.Impl

      peer.addWindowListener(this)
      peer.getRootPane.putClientProperty(Property, window)
      if (hasMenuBar) peer.setJMenuBar(window.handler.menuFactory.create(window).peer)

      def closeOperation        : Window.CloseOperation        = Window.CloseOperation(peer.getDefaultCloseOperation)
      def closeOperation_=(value: Window.CloseOperation): Unit = peer.setDefaultCloseOperation(value.id)

      def title                 : String                       = peer.getTitle
      def title_=         (value: String               ): Unit = peer.setTitle(value)

      def resizable             : Boolean                      = peer.isResizable
      def resizable_=     (value: Boolean              ): Unit = peer.setResizable(value)

      def alwaysOnTop           : Boolean                      = peer.isAlwaysOnTop
      def alwaysOnTop_=   (value: Boolean              ): Unit = peer.setAlwaysOnTop(value)

      def makeUndecorated(): Unit = peer.setUndecorated(true)

      def active: Boolean = peer.isActive

      def pack(): Unit = peer.pack()

      def dispose(): Unit = {
        peer.dispose()
        if (hasMenuBar) window.handler.menuFactory.destroy(window)
      }

      def front(): Unit = {
        if (!component.visible) component.visible = true
        peer.toFront()
      }

      //      def menu_=(value: MenuBar) { component.menuBar = value }

      // XXX TODO
      //      def componentAdded(e: ContainerEvent) {
      //        checkMenuBar(e)
      //      }
      //
      //      def componentRemoved(e: ContainerEvent) {
      //        checkMenuBar(e)
      //      }

      //      private def checkMenuBar(e: ContainerEvent) {
      //        val mb = e.getContainer.asInstanceOf[JMenuBar]
      //        val curr = component.menuBar
      //        if (mb.getMenuCount == 0) {
      //          if (curr == mb) {
      //            component.menuBar = swing.MenuBar.NoMenuBar
      //          }
      //        } else {
      //          if (curr == null) {
      //            component.peer.setJMenuBar(mb)
      //          }
      //        }
      //        val rp = component.peer.getRootPane
      //        rp.revalidate()
      //        rp.repaint()
      //      }

      def windowOpened      (e: WindowEvent): Unit = reactions(Window.Opened      (window))
      def windowClosing     (e: WindowEvent): Unit = reactions(Window.Closing     (window))
      def windowClosed      (e: WindowEvent): Unit = reactions(Window.Closed      (window))
      def windowIconified   (e: WindowEvent): Unit = reactions(Window.Iconified   (window))
      def windowDeiconified (e: WindowEvent): Unit = reactions(Window.Deiconified (window))
      def windowActivated   (e: WindowEvent): Unit = reactions(Window.Activated   (window))
      def windowDeactivated (e: WindowEvent): Unit = reactions(Window.Deactivated (window))
    }
  }
  trait Delegate {
    def component         : RootPanel
    var closeOperation    : Window.CloseOperation
    var title             : String
    var resizable         : Boolean
    var alwaysOnTop       : Boolean
//    def menu_=(value: MenuBar): Unit
    def active            : Boolean
    def pack()            : Unit
    def dispose()         : Unit
    def front()           : Unit
    def makeUndecorated() : Unit
    def reactions         : Reactions
  }
}
trait WindowStub extends Window {
  import de.sciss.desktop.impl.WindowImpl._

  protected def style: Window.Style = Window.Regular

  final protected def application: SwingApplication[_] = handler.application

  final def size                  : Dimension                     = component.size
  final def size_=          (value: Dimension             ): Unit = component.peer.setSize(value)

  final def bounds                : Rectangle                     = component.bounds
  final def bounds_=        (value: Rectangle             ): Unit = component.peer.setBounds(value)

  final def location              : Point                         = component.location
  final def location_=      (value: Point                 ): Unit = component.peer.setLocation(value)

  final def title                 : String                        = delegate.title
  final def title_=         (value: String                ): Unit = delegate.title = value

  final def resizable             : Boolean                       = delegate.resizable
  final def resizable_=     (value: Boolean               ): Unit = delegate.resizable = value

  final def closeOperation        : Window.CloseOperation         = delegate.closeOperation
  final def closeOperation_=(value: Window.CloseOperation ): Unit = delegate.closeOperation = value

  final def pack(): Unit = delegate.pack()

  final def contents              : immutable.Seq[Component]      = component.contents
  final def contents_=      (value: Component             ): Unit = component.contents = value

  final def active                : Boolean                       = delegate.active

  final def alwaysOnTop           : Boolean                       = delegate.alwaysOnTop
  final def alwaysOnTop_=   (value: Boolean               ): Unit = delegate.alwaysOnTop = value

  final def floating: Boolean = false   // XXX TODO

  final def front(): Unit = delegate.front()

  final def reactions             : Reactions                     = delegate.reactions

  final def visible               : Boolean                       = component.visible
  final def visible_=       (value: Boolean               ): Unit = component.visible = value

  private[this] var _dirty = false

  final def dirty: Boolean = _dirty

  final def dirty_=(value: Boolean): Unit =
    if (_dirty != value) {
      _dirty = value
      putClientProperty("windowModified", value)
    }

  final protected def putClientProperty(key: String, value: Any): Unit =
    component.peer.getRootPane.putClientProperty(key, value)

  protected def delegate: Delegate

  //  delegate.menu_=(handler.menuFactory.create(this))

  //  private def borrowMenuFrom(that: Window) {
  // 		if( borrowMenuBar && (barBorrower != that) ) {
  // 			if( (bar != null) && (barBorrower != null) ) {
  // 				barBorrower.setJMenuBar( bar );
  // 				bar = null;
  // 			}
  // 			barBorrower = that;
  // 			bar			= barBorrower == null ? null : barBorrower.getJMenuBar();
  // 			if( active ) {
  // 				if( barBorrower != null ) barBorrower.setJMenuBar( null );
  // 				if( jf != null ) {
  // 					jf.setJMenuBar( bar );
  // 				} else if( jif != null ) {
  // 					handler.getMainFrame().setJMenuBar( bar );
  // 				} else {
  // 					throw new IllegalStateException();
  // 				}
  // 			}
  // 		}
  // 	}

  private[this] var _file = Option.empty[File]

  final def file: Option[File] = _file

  final def file_=(value: Option[File]): Unit = {
    _file = value
    putClientProperty("Window.documentFile", value.orNull)
  }

  private[this] var _alpha = 1f

  final def alpha: Float = _alpha

  final def alpha_=(value: Float): Unit = {
    _alpha = value
    putClientProperty("Window.alpha", value)
    putClientProperty("apple.awt.draggableWindowBackground", false)
  }

  final def makeUnifiedLook(): Unit = putClientProperty("apple.awt.brushMetalLook", true)

  final def makeUndecorated(): Unit = delegate.makeUndecorated()

  final def component: RootPanel = delegate.component

  def dispose(): Unit = {
    handler.removeWindow(this)
    delegate.dispose()
  }

  final def showDialog[A](source: DialogSource[A]): A = handler.showDialog(Some(this), source)

  final def addAction(key: String, action: Action): Unit = {
    val a       = action.peer
    val stroke  = action.accelerator.getOrElse(
      throw new IllegalArgumentException(s"addAction($key, $action) - no accelerator found"))
    val root    = component.peer.getRootPane
    root.registerKeyboardAction(a, key, stroke, FocusType.Window.id)
  }

  final def addActions(entries: (String, Action)*): Unit =
    entries.foreach { case (key, action) => addAction(key, action) }

  final def bindMenu(path: String, action: Action): Unit = {
    val root  = handler.menuFactory
    root.get(path) match {
      case Some(it: Menu.ItemLike[_]) =>
        val src = it.action
        action.title            = src.title
        action.icon             = src.icon
        action.accelerator      = src.accelerator
        // putNoNullNull(src, a, Action.MNEMONIC_KEY)
        // action.mnemonic         = src.mnemonic
        // action.longDescription  = src.longDescription
        it.bind(this, action)

      case _ => sys.error(s"No menu item for path '$path'")
    }
  }

  final def bindMenus(entries: (String, Action)*): Unit =
    entries.foreach { case (key, action) => bindMenu(key, action) }
}
trait WindowImpl extends WindowStub {
  import de.sciss.desktop.impl.WindowImpl._

  protected final lazy val delegate: Delegate = {
    val screen = handler.usesScreenMenuBar
    // XXX TODO
    //    style match {
    //      case Window.Palette =>
    //
    //      case _ =>
    val res = if (handler.usesInternalFrames) {
      val jif = new JInternalFrame(null, true, true, true, true)
      // handler.getDesktop().add( jif )
      val hasMenuBar = style == Window.Regular
      Delegate.internalFrame(this, jif, hasMenuBar = hasMenuBar)

    } else {
      val f = new JFrame // swing.Frame
      val hasMenuBar = screen || (style == Window.Regular)
      Delegate.frame(this, f, screen = screen, hasMenuBar = hasMenuBar)
    }
    //			floating			= false;
    //     			tempFloating		= style == Window.Auxiliary && wh.usesFloating();
    //     			floating			= tempFloating;
    //    }

    // XXX TODO
    //    val borrowMenu = style == Window.Palette && {
    //      handler.usesInternalFrames || (!handler.usesFloatingPalettes && screen)
    //    }
    //
    //    if (borrowMenu) {
    //      borrowMenuFrom(handler.mainWindow)
    //      wh.addBorrowListener(this)
    //    } else if (ownMenuBar) {
    //    }
    res
  }

  handler.addWindow(this)
}