/*
 *  DynamicComponentImpl.scala
 *  (Desktop)
 *
 *  Copyright (c) 2013-2018 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.desktop
package impl

import java.awt
import java.awt.event.{ComponentEvent, ComponentListener, WindowEvent, WindowListener}
import javax.swing.event.{AncestorEvent, AncestorListener}

import scala.swing.Component

trait DynamicComponentImpl {
  _: Component =>

  private[this] var listening   = false
  private[this] var win         = Option.empty[awt.Window]

  protected def componentShown (): Unit
  protected def componentHidden(): Unit

  final def isListening: Boolean = listening

  // ---- constructor ----
  peer.addAncestorListener(listener)
  learnWindow(Option(peer.getTopLevelAncestor))

  private def startListening(): Unit =
    if (!listening) {
      listening = true
      componentShown()
    }

  private def stopListening(): Unit =
    if (listening) {
      listening = false
      componentHidden()
    }

  private def forgetWindow(): Unit =
    win.foreach { w =>
      w.removeWindowListener(listener)
      w.removeComponentListener(listener)
      win = None
      stopListening()
    }

  private def learnWindow(c: Option[awt.Container]): Unit =
    c match {
      case Some(w: awt.Window) =>
        win = Some(w)
        w.addWindowListener(listener)
        w.addComponentListener(listener)
        if (w.isShowing) startListening()

      case _ =>
    }

  private object listener extends WindowListener with ComponentListener with AncestorListener {
    def windowOpened     (e: WindowEvent): Unit = startListening()
 		def windowClosed     (e: WindowEvent): Unit = stopListening ()

    def windowClosing    (e: WindowEvent): Unit = ()
    def windowIconified  (e: WindowEvent): Unit = ()
    def windowDeiconified(e: WindowEvent): Unit = ()
    def windowActivated  (e: WindowEvent): Unit = ()
    def windowDeactivated(e: WindowEvent): Unit = ()

    def componentShown  (e: ComponentEvent): Unit = startListening()
    def componentHidden (e: ComponentEvent): Unit = stopListening ()

    def componentResized(e: ComponentEvent): Unit = ()
    def componentMoved  (e: ComponentEvent): Unit = ()

    def ancestorAdded(e: AncestorEvent): Unit = {
      val c = Option(e.getComponent.getTopLevelAncestor)
      if (c != win) {
        forgetWindow()
        learnWindow(c)
      }
    }

    def ancestorRemoved(e: AncestorEvent): Unit = forgetWindow()

    def ancestorMoved  (e: AncestorEvent): Unit = ()
  }

//	def remove(): Unit = {
//		removeAncestorListener(listener)
//		forgetWindow()
//	}
}