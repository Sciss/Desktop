/*
 *  MenuImpl.scala
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

import javax.swing.KeyStroke
import scala.swing.event.Key
import swing.Action
import de.sciss.swingplus.PopupMenu

private[desktop] object MenuImpl {
  i =>

  // ---- constructors ----
  import Menu.Attributes

  def itemApply(key: String, action: Action): Menu.Item = new Item(key, action)
  def itemApply(key: String)(attr: Attributes)(action: => Unit): Menu.Item =
    new Item(key, i.action(attr.text, attr.keyStroke)(action))

  def itemApply(key: String, attr: Attributes): Menu.Item = {
    val a     = i.noAction(attr.text, attr.keyStroke)
    a.enabled = false
    new Item(key, a)
  }

  def checkBoxApply(key: String, action: Action): Menu.CheckBox = new CheckBox(key, action)
  def checkBoxApply(key: String)(attr: Attributes)(action: => Unit): Menu.CheckBox =
    new CheckBox(key, i.action(attr.text, attr.keyStroke)(action))

  def checkBoxApply(key: String, attr: Attributes): Menu.CheckBox = {
    val a     = i.noAction(attr.text, attr.keyStroke)
    a.enabled = false
    new CheckBox(key, a)
  }

  def groupApply(key: String, action: Action): Menu.Group = new Group(key, action)
  def groupApply(key: String)(text: String)(action: => Unit): Menu.Group =
    new Group(key, i.action(text, None)(action))
  def groupApply(key: String, text: String): Menu.Group =
    new Group(key, i.noAction(text, None))

  def rootApply(): Menu.Root = new Root

  def popupApply(): Menu.Popup = new Popup

  def aboutApply(app: SwingApplication)(action: => Unit): Menu.Item = {
    val supported = Desktop.platform.setAboutHandler(action)
    val text      = s"About ${app.name}" // XXX TODO: localization? shortcuts?
    val item      = Menu.Item("about")(text)(action)
    item.visible  = !supported
    item
  }

  def prefsApply(app: SwingApplication)(action: => Unit): Menu.Item = {
    val supported = Desktop.platform.setPreferencesHandler(action)
    // XXX TODO: localization? shortcuts?
    // cf. http://kb.mozillazine.org/Menu_differences_in_Windows,_Linux,_and_Mac
    val attr: Attributes = if (Desktop.isLinux)
      "Preferences" -> (KeyStrokes.menu1 + Key.Comma)   // this is not standard, but useful
    else if (Desktop.isMac)
      "Preferences..." -> (KeyStrokes.menu1 + Key.Comma)
    else
      "Options..."  // XXX TODO - is there a default shortcut?

    val item      = Menu.Item("preferences")(attr)(action)
    item.visible  = !supported
    item
  }

  def quitApply(app: SwingApplication): Menu.Item = {
    val supported = Desktop.isQuitSupported
    val attr: Attributes = if (Desktop.isWindows)
      "Exit" -> (KeyStrokes.menu1 + Key.Q)  // isn't it (KeyStrokes.alt + Key.F4)? Most programs seem to use Ctrl-Q
    else
      "Quit" -> (KeyStrokes.menu1 + Key.Q) // XXX TODO: localization?

    val item      = Menu.Item("quit")(attr) {
      if (Desktop.mayQuit()) app.quit()
    }
    item.visible  = !supported
    item
  }

  // ---- util ---

  def action(text: String, stroke: Option[KeyStroke])(body: => Unit): Action = {
    val res = Action(text)(body)
    res.accelerator = stroke
    res
  }

  def noAction(text: String, stroke: Option[KeyStroke]): Action = new Action(text) {
    accelerator = stroke

    override def toString = s"proxy($title)"
    def apply() = ()
  }

  //  private[this] var _checkBoxSelected = false
  //  def checkBoxSelected: Boolean = _checkBoxSelected

  // ---- node ----

  private trait Node {
    protected def key: String
    protected def prefix: String

    override def toString = s"Menu.$prefix($key)"
  }

  // ---- realizable tracking ----
  private trait Realizable[C <: swing.Component] extends Node {
    private var mapRealized = Map.empty[Window, C]

    final protected def getRealized(w: Window): Option[C] = mapRealized.get(w)
    final protected def realizedIterator: Iterator[(Window, C)] = mapRealized.iterator

    final protected def addRealized(w: Window, c: C): Unit =
      mapRealized += w -> c // Realized(w, c)

    final protected def removeRealized(w: Window): Unit =
      mapRealized -= w
  }

  private trait CanEnable {
    var enabled: Boolean

    final def enable(): this.type = {
      enabled = true
      this
    }
    final def disable(): this.type = {
      enabled = false
      this
    }
  }

  // ---- item ----

  private trait ItemLike[C <: swing.MenuItem] extends CanEnable with Realizable[C] {
    protected def action: Action

    private var mapWindowActions  = Map.empty[Window, Action] withDefaultValue action
    private var _visible          = true

    final def enabled = action.enabled
    final def enabled_=(value: Boolean): Unit = action.enabled = value

    final def visible = _visible
    final def visible_=(value: Boolean): Unit =
      if (_visible != value) {
        _visible = value
        realizedIterator.foreach {
          case (_, c) => c.visible = value
        }
      }

    final protected def actionFor(w: Window): Action = mapWindowActions(w)

    final protected def putAction(w: Window, action: Action): Unit =
      mapWindowActions += w -> action

    final protected def removeAction(w: Window): Unit =
      mapWindowActions -= w

    final def bind(w: Window, action: Action): Unit = {
      getRealized(w).foreach { mi =>
        mi.action = action
      }
      putAction(w, action)
    }
  }

  // private sealed trait LeafItemLike[C <: swing.MenuItem] extends ItemLike[C]

  private final class Item(val key: String, val action: Action) extends ItemLike[swing.MenuItem] with Menu.Item {
    protected def prefix = "Item"

    def create(w: Window): swing.MenuItem = {
      val c = new swing.MenuItem(actionFor(w))
      if (!visible) c.visible = false
      addRealized(w, c)
      c
    }

    def destroy(w: Window): Unit = {
      removeRealized(w)
      removeAction(w)
    }
  }

  private final class CheckBox(val key: String, val action: Action)
    extends ItemLike[swing.CheckMenuItem] with Menu.CheckBox {

    protected def prefix = "CheckBox"

    def apply(window: Window): swing.CheckMenuItem =
      getRealized(window).getOrElse(throw new IllegalArgumentException(s"Window $window not realized"))

    def create(w: Window): swing.CheckMenuItem = {
      val c = new swing.CheckMenuItem("")
      c.action = actionFor(w)
      if (!visible) c.visible = false
      addRealized(w, c)
      c
    }

    def destroy(w: Window): Unit = {
      removeRealized(w)
      removeAction(w)
    }
  }

  // ---- group ----

  private final class NodeProxy(val window: Option[Window]) {
    var seq   = Vector.empty[Menu.Element]
    var map   = Map.empty[String, Menu.NodeLike]

    override def toString = s"NodeProxy($window)@${hashCode().toHexString}"

    def create(c: swing.SequentialContainer, w: Window): Unit = {
      if (window.isDefined) require(window.get == w)  // XXX TODO -- correct?

      seq.foreach { n => c.contents += n.create(w)}
    }

    def destroy(w: Window): Unit = {
      if (window.isDefined) require(window.get == w)  // XXX TODO -- correct?

      seq.foreach(_.destroy(w))
    }
  }

  private trait GroupLike[C <: swing.Component with swing.SequentialContainer]
    extends Realizable[C] {

    private var proxies       = Map.empty[Window, NodeProxy]
    private val defaultProxy  = new NodeProxy(None)

    private def added(p: NodeProxy, n: Menu.Element, idx: Int): Unit = {
      val isDefault = p.window.isEmpty
      realizedIterator.foreach { case (w, r) =>
        if (isDefault || p.window == Some(w)) {
          val comp = n.create(w)
          if (idx < 0)
            r.contents += comp
          else
            r.contents.insert(idx, comp)
        }
      }
    }

    private def proxy(wo: Option[Window]): NodeProxy = wo.fold(defaultProxy) { w =>
      proxies.getOrElse(w, {
        val p = new NodeProxy(wo)
        proxies += w -> p
        p
      })
    }

    final protected def createProxy(w: Window, component: C): Unit = {
      defaultProxy.create(component, w)
      proxies.get(w).foreach(_.create(component, w)) // XXX TODO
    }

    final protected def destroyProxy(w: Window): Unit = {
      defaultProxy.destroy(w)
      proxies.get(w).foreach { p =>
        p.destroy(w)
        if (p.seq.isEmpty) proxies -= w
      }
    }

    private def add(p: NodeProxy, elem: Menu.Element, idx: Int): Unit = {
      elem match {
        case n: Menu.NodeLike =>
          require(!p.map.contains(n.key), "Element already added")
          // println(s"Adding ${n.key} -> $n to $p")
          p.map += n.key -> n
        case _ =>
      }
      if (idx < 0)
        p.seq :+= elem
      else if (idx == 0)
        p.seq +:= elem
      else
        p.seq = p.seq.patch(idx, elem :: Nil, 0)

      added(p, elem, idx)
    }

    // adds window specific action to the tail
    final def add(w: Option[Window], elem: Menu.Element): this.type = {
      add(proxy(w), elem, -1)
      this
    }

    // adds to the tail
    final def add(elem: Menu.Element): this.type = {
      add(defaultProxy, elem, -1)
      this
    }

    //    final def insertAfter(pred: Menu.Element, w: Option[Window], elem: Menu.Element): this.type = {
    //      val np = proxy(w)
    //      add(proxy(w), elem, np.seq.indexOf(pred) + 1)
    //      this
    //    }
    //
    //    final def insertAfter(pred: Menu.Element, elem: Menu.Element): this.type = {
    //      add(defaultProxy, elem, defaultProxy.seq.indexOf(pred) + 1)
    //      this
    //    }
    //
    //    final def insertBefore(succ: Menu.Element, w: Option[Window], elem: Menu.Element): this.type = {
    //      val np = proxy(w)
    //      add(proxy(w), elem, np.seq.indexOf(succ))
    //      this
    //    }
    //
    //    final def insertBefore(succ: Menu.Element, elem: Menu.Element): this.type = {
    //      add(defaultProxy, elem, defaultProxy.seq.indexOf(succ))
    //      this
    //    }

    private def get(w: Option[Window], p: NodeProxy, path: String): Option[Menu.NodeLike] = {
      val i = path.indexOf('.')
      val k = if (i < 0) path else path.substring(0, i)
      val e = p.map.get(k)
      // println(s"In $p look for key $k yields $e")
      if (i < 0) e else e match {
        case Some(g: Menu.Group) => g.get(w, path.substring(i + 1))
        case _ => None
      }
    }

    final def get(w: Option[Window], path: String): Option[Menu.NodeLike] = get(w, proxy(w), path)
    final def get(path: String): Option[Menu.NodeLike] = get(None, defaultProxy, path)
  }

  private final class Group(val key: String, val action: Action)
    extends GroupLike[swing.Menu] with ItemLike[swing.Menu] with Menu.Group {

    protected def prefix = "Group"

    def create(w: Window): swing.Menu = {
      val c = createComponent(actionFor(w))
      if (!visible) c.visible = false
      addRealized(w, c)
      createProxy(w, c)
      c
    }

    def destroy(w: Window): Unit = {
      removeRealized(w)
      removeAction(w)
      destroyProxy(w)
    }

    def addLine(): this.type = add(Menu.Line)

    private def createComponent(a: Action): swing.Menu = {
      val res     = new swing.Menu(a.title)
      res.action  = a
      res
    }
  }

  // ---- root ----

  private sealed trait RootLike[C <: swing.Component with swing.SequentialContainer]
    extends GroupLike[C] with Menu.GroupLike[C] with CanEnable {

    private var _enabled = true

    protected def createEmptyRoot(): C

    final def create(w: Window): C = {
      val res = createEmptyRoot()
      addRealized(w, res)
      createProxy(w, res)
      if (!enabled) res.enabled = false
      res
    }

    final def destroy(w: Window): Unit = {
      removeRealized(w)
      destroyProxy(w)
    }

    final def enabled = _enabled
    final def enabled_=(value: Boolean): Unit = { // XXX TODO: should we filter _enabled != value ?
      _enabled = value
      realizedIterator.foreach(_._2.enabled = value)
    }
  }

  private final class Root extends RootLike[swing.MenuBar] with Menu.Root {
    def key = "root"
    protected def prefix = "Root"
    protected def createEmptyRoot(): swing.MenuBar = new swing.MenuBar
  }

  private final class Popup extends RootLike[PopupMenu] with Menu.Popup {
    def key = "popup"
    protected def prefix = "Popup"
    protected def createEmptyRoot(): PopupMenu = new PopupMenu
  }
}