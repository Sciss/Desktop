/*
 *  MenuImpl.scala
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
package impl

import javax.swing.KeyStroke
import swing.Action
import scalaswingcontrib.PopupMenu

private[desktop] object MenuImpl {
  i =>

  // ---- constructors ----
  import Menu.Item.Attributes

  def itemApply(key: String, action: Action): Menu.Item = new Item(key, action)
  def itemApply(key: String)(attr: Attributes)(action: => Unit): Menu.Item =
    new Item(key, i.action(attr.text, attr.keyStroke)(action))

  def itemApply(key: String, attr: Attributes): Menu.Item = {
    val a     = i.noAction(attr.text, attr.keyStroke)
    a.enabled = false
    new Item(key, a)
  }

  def groupApply(key: String, action: Action): Menu.Group = new Group(key, action)
  def groupApply(key: String)(text: String)(action: => Unit): Menu.Group =
    new Group(key, i.action(text, None)(action))
  def groupApply(key: String, text: String): Menu.Group =
    new Group(key, i.noAction(text, None))

  def rootApply(): Menu.Root = new Root

  def popupApply(): Menu.Popup = new Popup

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

    private def added(p: NodeProxy, n: Menu.Element): Unit = {
      val isDefault = p.window.isEmpty
      realizedIterator.foreach { case (w, r) =>
        if (isDefault || p.window == Some(w)) r.contents += n.create(w)
      }
    }

    private def proxy(wo: Option[Window]): NodeProxy = wo match {
      case Some(w) =>
        proxies.getOrElse(w, {
          val p = new NodeProxy(wo)
          proxies += w -> p
          p
        })
      case None => defaultProxy
    }

    final protected def createProxy(w: Window, component: C): Unit = {
      defaultProxy.create(component, w)
      proxies.get(w).foreach(_.create(component, w)) // XXX TODO
    }

    final protected def destroyProxy(w: Window): Unit = {
      defaultProxy.destroy(w)
      proxies.get(w).foreach { p =>
        p.destroy(w)
     	  if( p.seq.isEmpty ) proxies -= w
      }
    }

    private def add(p: NodeProxy, elem: Menu.Element): Unit = {
      elem match {
        case n: Menu.NodeLike =>
          require(!p.map.contains(n.key), "Element already added")
          // println(s"Adding ${n.key} -> $n to $p")
          p.map += n.key -> n
        case _ =>
      }
      p.seq :+= elem
      added(p, elem)
    }

    // adds window specific action to the tail
    final def add(w: Option[Window], elem: Menu.Element): this.type = {
      add(proxy(w), elem)
      this
    }

    // adds to the tail
    final def add(n: Menu.Element): this.type = {
      add(defaultProxy, n)
      this
    }

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
    protected def createEmptyRoot() = new swing.MenuBar
  }

  private final class Popup extends RootLike[PopupMenu] with Menu.Popup {
    def key = "popup"
    protected def prefix = "Popup"
    protected def createEmptyRoot() = new PopupMenu
  }
}