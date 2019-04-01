/*
 *  Util.scala
 *  (Desktop)
 *
 *  Copyright (c) 2013-2019 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.desktop

import java.awt.GraphicsEnvironment
import java.awt.event.KeyEvent
import javax.swing.event.{AncestorEvent, AncestorListener}
import javax.swing.{JComponent, KeyStroke, Timer}

import de.sciss.swingplus.DoClickAction

import scala.swing.event.Key
import scala.swing.{AbstractButton, Action, Component, Point, Rectangle, Swing, TabbedPane}

object Util {
  /** Positions a window on the center of the default screen device. */
  def centerOnScreen(w: Window): Unit = placeWindow(w, 0.5f, 0.5f, 0)

  /** Executes a code block once after a given delay on the event-dispatch-thread. */
  def delay(millis: Int)(block: => Unit): Unit = {
    val timer = new Timer(millis, Swing.ActionListener(_ => block))
    timer.setRepeats(false)
    timer.start()
  }

  /** Sets a component's minimum and maximum size to match the preferred size. */
  def fixSize(c: Component): Unit = {
    val d = c.preferredSize
    c.preferredSize = d
    c.minimumSize   = d
    c.maximumSize   = d
  }

  /** Sets the minimum, maximum and preferred width of a component.
    *
    * @param width  the width to use or `-1` to query the preferred width instead.
    */
  def fixWidth(c: Component, width: Int = -1): Unit = {
    val pref        = c.preferredSize
    val w           = if (width < 0) pref.width else width
    val min         = c.minimumSize
    val max         = c.maximumSize
    pref.width      = w
    min.width       = w
    max.width       = w
    c.preferredSize = pref
    c.minimumSize   = min
    c.maximumSize   = max
  }


  /** Sets the minimum, maximum and preferred width of a number of components
    * to the maximum of their individual preferred widths.
    */
  def sameWidths(c: Component*): Unit = {
    var w = 0
    c.foreach { comp =>
      val pref = comp.preferredSize
      w = math.max(w, pref.width)
    }
    c.foreach { comp =>
      import comp._
      val pref      = preferredSize
      val min       = minimumSize
      val max       = maximumSize
      pref.width    = w
      min .width    = w
      max .width    = w
      preferredSize = pref
      minimumSize   = min
      maximumSize   = max
    }
  }

  /** Returns the maximum bounds a window should have on the default screen device. */
  def maximumWindowBounds: Rectangle = {
    val ge  = GraphicsEnvironment.getLocalGraphicsEnvironment
    ge.getMaximumWindowBounds
  }

  /** Positions a window on the default screen device.
    *
    * @param horizontal the horizontal placement from 0.0 (left-most) to 1.0 (right-most)
    * @param vertical   the vertical placement from 0.0 (top-most) to 1.0 (bottom-most)
    * @param padding    additional padding in pixels from the screen's margins. For example,
    *                   if horizontal is `1.0` and padding is `40`, the window will be
    *                   placed so that its right border is spaced 40 pixels from the right
    *                   margin of the screen.
    */
  def placeWindow(w: Window, horizontal: Float, vertical: Float, padding: Int): Unit = {
    val bs  = maximumWindowBounds
    val b   = w.size
    val x   = (horizontal * (bs.width  - padding * 2 - b.width )).toInt + bs.x + padding
    val y   = (vertical   * (bs.height - padding * 2 - b.height)).toInt + bs.y + padding
    w.location = new Point(x, y)
  }

  def wordWrap(s: String, margin: Int = 80): String = {
    val sz = s.length
    if (sz <= margin) return s
    var i = 0
    val sb = new StringBuilder
    while (i < sz) {
      val j = s.lastIndexOf(" ", i + margin)
      val found = j > i
      val k = if (found) j else i + margin
      sb.append(s.substring(i, math.min(sz, k)))
      i = if (found) k + 1 else k
      if (i < sz) sb.append('\n')
    }
    sb.toString()
  }

  /** Formats the name of the class of a value
    * by inserting space characters at the 'camel' positions.
    */
  def formatClassName(x: Class[_]): String = {
    val cn0 = x.getName
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

  /** Creates a formatted text from an exception, useful for displaying in a dialog.
    *
    * @param e                the exception to format
    * @param margin           the word wrapping margin for the exception's message
    * @param stackTraceLines  the number of stack trace elements to include. if zero,
    *                         no stack trace will be included.
    */
  def formatException(e: Throwable, margin: Int = 80, stackTraceLines: Int = 10): String = {
    val name    = if (e == null) "Exception" else formatClassName(e.getClass)
    val strBuf  = new StringBuilder(name)
    val message = if (e == null) null else {
      val loc = e.getLocalizedMessage
      if (loc == null) e.getMessage else loc
    }
    if (message != null) {
      strBuf.append(":\n")
      strBuf.append(wordWrap(message, margin = margin))
    }
    if (stackTraceLines > 0) {
      val stackS = e.getStackTrace.iterator.take(stackTraceLines).map("   at " + _).mkString("\n")
      strBuf.append("\n")
      strBuf.append(stackS)
    }
    strBuf.result()
  }

  /** Requests that a components gets focus once it is made visible. */
  def setInitialFocus(c: Component): Unit = {
    val adapter = new AncestorListener {
      def ancestorAdded(e: AncestorEvent): Unit = {
        c.peer.removeAncestorListener(this)
        c.requestFocusInWindow()
      }

      def ancestorMoved  (e: AncestorEvent): Unit = ()
      def ancestorRemoved(e: AncestorEvent): Unit = ()
    }

    c.peer.addAncestorListener (adapter)
  }

  /** Adds a global key-triggered click action to a button. The key
    * is active if the action appears in the focused window. The added action
    * simulates a button click.
    */
  def addGlobalKey(b: AbstractButton, keyStroke: KeyStroke): Unit = {
    val click = DoClickAction(b)
    b.peer.getActionMap.put("click", click.peer)
    b.peer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "click")
  }

  /** Same as `addGlobalKey`, but only performs action if the button is
    * currently showing (e.g. in the currently showing tabbed pane page).
    */
  def addGlobalKeyWhenVisible(b: AbstractButton, keyStroke: KeyStroke): Unit = {
    val click = Action(null) {
      if (b.showing) b.doClick()
    }
    b.peer.getActionMap.put("click", click.peer)
    b.peer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "click")
  }

  /** Adds a global action to a component. The key
    * is active if the component appears in the focused window.
    */
  def addGlobalAction(c: Component, name: String, keyStroke: KeyStroke)(body: => Unit): Unit = {
    val a = Action(null)(body)
    c.peer.getActionMap.put(name, a.peer)
    c.peer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, name)
  }

  /** Adds alt-left/right key control to a tabbed pane. */
  def addTabNavigation(tabs: TabbedPane): Unit = {
    addGlobalAction(tabs, "prev", KeyStroke.getKeyStroke(Key.Left.id, Key.Modifier.Alt)) {
      val sel   = tabs.selection
      val idx   = sel.index - 1
      sel.index = if (idx >= 0) idx else tabs.pages.size - 1
    }
    addGlobalAction(tabs, "next", KeyStroke.getKeyStroke(Key.Right.id, Key.Modifier.Alt)) {
      val sel   = tabs.selection
      val idx   = sel.index + 1
      sel.index = if (idx < tabs.pages.size) idx else 0
    }
  }

  /** Human-readable text representation of a key stroke. */
  def keyStrokeText(stroke: KeyStroke): String = {
    val mod = stroke.getModifiers
    val sb  = new StringBuilder
    if (mod > 0) {
      sb.append(KeyEvent.getKeyModifiersText(mod))
      sb.append('+')
    }
    sb.append(KeyEvent.getKeyText(stroke.getKeyCode))
    sb.result()
  }
}