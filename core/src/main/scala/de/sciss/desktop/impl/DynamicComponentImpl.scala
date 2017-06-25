/*
 *  DynamicComponentImpl.scala
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

package de.sciss.desktop
package impl

import java.awt.event.{ComponentEvent, ComponentListener, WindowListener, WindowEvent}
import javax.swing.event.{AncestorEvent, AncestorListener}
import java.awt
import scala.swing.Component

trait DynamicComponentImpl {
  _: Component =>

  private var listening   = false
  private var win         = Option.empty[awt.Window]

  protected def componentShown (): Unit
  protected def componentHidden(): Unit

  final def isListening = listening

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

    def windowClosing    (e: WindowEvent) = ()
    def windowIconified  (e: WindowEvent) = ()
    def windowDeiconified(e: WindowEvent) = ()
    def windowActivated  (e: WindowEvent) = ()
    def windowDeactivated(e: WindowEvent) = ()

    def componentShown  (e: ComponentEvent): Unit = startListening()
    def componentHidden (e: ComponentEvent): Unit = stopListening ()

    def componentResized(e: ComponentEvent) = ()
    def componentMoved  (e: ComponentEvent) = ()

    def ancestorAdded(e: AncestorEvent): Unit = {
      val c = Option(e.getComponent.getTopLevelAncestor)
      if (c != win) {
        forgetWindow()
        learnWindow(c)
      }
    }

    def ancestorRemoved(e: AncestorEvent): Unit = forgetWindow()

    def ancestorMoved  (e: AncestorEvent) = ()
  }

//	def remove(): Unit = {
//		removeAncestorListener(listener)
//		forgetWindow()
//	}
}