/*
 *  Menu.scala
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

import swing.{Action, Component}
import javax.swing.KeyStroke

import impl.{MenuImpl => Impl}
import scalaswingcontrib.PopupMenu

object Menu {
  sealed trait Element {
    def create(window: Window): Component
    def destroy(window: Window): Unit
  }
  sealed trait NodeLike extends Element {
    def key: String
    var enabled: Boolean
    def enable (): this.type
    def disable(): this.type
  }
  trait Node[+C <: Component] extends NodeLike {
    def create(window: Window): C
  }
  object Line extends Element {
    def create (window: Window) = new swing.Separator
    def destroy(window: Window) = ()
  }

  object Item {
    def apply(key: String, action: Action): Item = Impl.itemApply(key, action)
    def apply(key: String)(attr: Attributes)(action: => Unit): Item =
      Impl.itemApply(key)(attr)(action)

    def apply(key: String, attr: Attributes): Item = Impl.itemApply(key, attr)

    /** Creates a special 'About' menu item. If the platform has a special
      * place for this item, the returned object indicates this by being `!visible`.
      * In that case the call site may omit adding the item to the regular menu structure.
      * If the item is visible, i.e. the platform does not have special support for it,
      * it should be added to the end of the last menu group labelled `"Help"`.
      *
      * The item's key will be `"about"`.
      *
      * @param app      the Swing application which is used to determine the item name.
      * @param action   the action to execute when the menu item is selected
      * @return         the menu item, possibly a dummy in which case its `visible` attribute is `false`
      */
    def About(app: SwingApplication)(action: => Unit): Item = Impl.aboutApply(app)(action)

    /** Creates a special 'Preferences' menu item. If the platform has a special
      * place for this item, the returned object indicates this by being `!visible`.
      * In that case the call site may omit adding the item to the regular menu structure.
      * If the item is visible, i.e. the platform does not have special support for it,
      * it should be added to `"Edit"` menu on Linux or the `"Tools"` menu on Windows.
      *
      * The item's key will be `"preferences"`.
      *
      * @param app      the Swing application which is used to determine the item name.
      * @param action   the action to execute when the menu item is selected
      * @return         the menu item, possibly a dummy in which case its `visible` attribute is `false`
      */
    def Preferences(app: SwingApplication)(action: => Unit): Item = Impl.prefsApply(app)(action)

    /** Creates a special 'Quit' menu item. If the platform has a special
      * place for this item, the returned object indicates this by being `!visible`.
      * In that case the call site may omit adding the item to the regular menu structure.
      * If the item is visible, i.e. the platform does not have special support for it,
      * it should be added to `"File"` menu.
      *
      * The item is automatically associated with an action which calls `Desktop.mayQuit()`, conditionally
      * followed by `app.quit()`.
      *
      * The item's key will be `"quit"`.
      *
      * @param app      the Swing application which is used to determine the item name.
      * @return         the menu item, possibly a dummy in which case its `visible` attribute is `false`
      */
    def Quit(app: SwingApplication): Item = Impl.quitApply(app)

    object Attributes {
      implicit final class TextOnly(val text: String) extends Attributes {
        def keyStroke   = None
      }
      implicit final class TextAndKeyStroke(tup: (String, KeyStroke)) extends Attributes {
        def text        = tup._1
        def keyStroke   = Some(tup._2)
      }
    }
    sealed trait Attributes {
      def text: String
      def keyStroke: Option[KeyStroke]
    }
  }
  sealed trait ItemLike[+C <: swing.MenuItem] extends Node[C] {
    def action: Action
    var visible: Boolean
    def bind(window: Window, action: Action): Unit
  }
  trait Item extends Node[swing.MenuItem] with ItemLike[swing.MenuItem]

  object Group {
    def apply(key: String, action: Action): Group = Impl.groupApply(key, action)
    def apply(key: String)(text: String)(action: => Unit): Group = Impl.groupApply(key)(text)(action)
    def apply(key: String, text: String): Group = Impl.groupApply(key, text)
  }
  trait GroupLike[+C <: Component with swing.SequentialContainer] extends Node[C]{
    def add(window: Option[Window], elem: Element): this.type
    def add(elem: Element): this.type
    def get(window: Option[Window], path: String): Option[NodeLike]
    def get(path: String): Option[NodeLike]
  }
  trait Group extends GroupLike[swing.Menu] with ItemLike[swing.Menu] {
    def addLine(): this.type
  }

  object Root {
    def apply(): Root = Impl.rootApply()
  }
  trait Root extends GroupLike[swing.MenuBar]

  object Popup {
    def apply(): Popup = Impl.popupApply()
  }
  trait Popup extends GroupLike[PopupMenu]

  def proxy(attr: Item.Attributes): Action = {
    val a = Impl.noAction(attr.text, attr.keyStroke)
    a.enabled = false
    a
  }
}
