/*
 *  MenuImpl.scala
 *  (Desktop)
 *
 *  Copyright (c) 2013 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU General Public License
 *	as published by the Free Software Foundation; either
 *	version 2, june 1991 of the License, or (at your option) any later version.
 *
 *	This software is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *	General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public
 *	License (gpl.txt) along with this software; if not, write to the Free Software
 *	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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

//  private final val ID_KEY = "de.sciss.gui.Menu.id"

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
//    res.peer.putValue(ID_KEY, key)
    res.accelerator = stroke
    res
  }

  def noAction(text: String, stroke: Option[KeyStroke]): Action = new Action(text) {
//  peer.putValue(ID_KEY, key)
    accelerator = stroke
//    enabled     = false

    override def toString = s"proxy($title)"
    def apply() {}
  }

//  private final class ActionImpl(key: String, text: String, stroke: Option[KeyStroke], body: => Unit)
//    extends Action(text) {
//
//    accelerator = stroke
//    peer.putValue(ID_KEY, key)
//
//    def apply() { body }
//  }

  // ---- node ----

//  private final case class Realized[C](window: Frame, component: C)

  private trait Node {
    _: Menu.NodeLike =>

    protected def prefix: String

    override def toString = s"Menu.$prefix($key)"
  }

  // ---- realizable tracking ----
  private trait Realizable[C <: swing.Component] extends Node {
    _: Menu.NodeLike =>

    private var mapRealized = Map.empty[Window, C]

    final protected def getRealized(w: Window): Option[C] = mapRealized.get(w)
    final protected def realizedIterator: Iterator[(Window, C)] = mapRealized.iterator

    final protected def addRealized(w: Window, c: C) {
      mapRealized += w -> c // Realized(w, c)
    }

    final protected def removeRealized(w: Window) {
      mapRealized -= w
    }
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
    _: Menu.ItemLike[C] =>

    private var mapWindowActions = Map.empty[Window, Action] withDefaultValue action
    final def enabled = action.enabled
    final def enabled_=(value: Boolean) { action.enabled = value }

    final protected def actionFor(w: Window): Action = mapWindowActions(w)

    final def setAction(w: Window, action: Action) {
      if (mapWindowActions.contains(w)) throw new IllegalStateException("Window specific action already set")
      mapWindowActions += w -> action
    }

    final def setAction(w: Window)(body: => Unit) {
      setAction(w, i.action(action.title, action.accelerator)(body))
    }

    final def clearAction(w: Window) {
      mapWindowActions -= w
    }
  }

  private final class Item(val key: String, val action: Action) extends ItemLike[swing.MenuItem] with Menu.Item {
    protected def prefix = "Item"

    def create(w: Window): swing.MenuItem = {
      val c = new swing.MenuItem(actionFor(w))
      addRealized(w, c)
      c
    }

    def destroy(w: Window) {
      removeRealized(w)
      clearAction(w)
    }
  }

  // ---- group ----

  private final class NodeProxy(val window: Option[Window]) {
    var seq   = Vector.empty[Menu.Element]
    var map   = Map.empty[String, Menu.NodeLike]

    override def toString = s"NodeProxy($window)@${hashCode().toHexString}"

    def create(c: swing.SequentialContainer, w: Window) {
      if (window.isDefined) require(window.get == w)  // XXX TODO -- correct?

      seq.foreach { n => c.contents += n.create(w)}
    }

    def destroy(w: Window) {
      if (window.isDefined) require(window.get == w)  // XXX TODO -- correct?

      seq.foreach(_.destroy(w))
    }
  }

  private trait GroupLike[C <: swing.Component with swing.SequentialContainer]
    extends Realizable[C] {
    _: Menu.NodeLike =>

    private var proxies       = Map.empty[Window, NodeProxy]
    private val defaultProxy  = new NodeProxy(None)

    private def added(p: NodeProxy, n: Menu.Element) {
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

    final protected def createProxy(w: Window, component: C) {
      defaultProxy.create(component, w)
      proxies.get(w).foreach(_.create(component, w)) // XXX TODO
    }

    final protected def destroyProxy(w: Window) {
      defaultProxy.destroy(w)
      proxies.get(w).foreach { p =>
        p.destroy(w)
     	  if( p.seq.isEmpty ) proxies -= w
      }
    }

    private def add(p: NodeProxy, elem: Menu.Element) {
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

    final def bind(child: String, window: Window, action: Action) {
      val p = proxies.get(window).getOrElse(throw new NoSuchElementException(s"Window $window was not yet realized"))
      val c = p.map.getOrElse(child, throw new NoSuchElementException(s"Child $child not found"))

      ???
    }

//	// inserts at given index
//	private void add( NodeProxy p, Menu.Node n, int index )
//	{
//		if( p.mapElements.put( n.getID(), n ) != null ) throw new IllegalArgumentException( "Element already added : " + n );
//
//		Realized r;
//		final boolean isDefault = p.w == null;
//
//		p.collElements.add( index, n );
//
//		for( Iterator iter = mapRealized.values().iterator(); iter.hasNext(); ) {
//			r = (Realized) iter.next();
//			if( isDefault || (p.w == r.w) ) {
//				r.c.add( n.create( r.w ), index + (isDefault ? 0 : defaultProxy.size()) );
//			}
//		}
//	}
  }

  private final class Group(val key: String, val action: Action)
    extends GroupLike[swing.Menu] with ItemLike[swing.Menu] with Menu.Group {

    protected def prefix = "Group"

    def create(w: Window): swing.Menu = {
      val c = createComponent(actionFor(w))
      addRealized(w, c)
      createProxy(w, c)
      c
    }

    def destroy(w: Window) {
      removeRealized(w)
      clearAction(w)
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

    final def destroy(w: Window) {
      removeRealized(w)
      destroyProxy(w)
    }

    final def enabled = _enabled
    final def enabled_=(value: Boolean) {
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

//  {
//  	private static int uniqueID = 0;
//
//  	public MenuSeparator()
//  	{
//  		super( "_" + String.valueOf( uniqueID++ ), (Action) null );
//  	}
//
//  	public void setEnabled( boolean b ) { /* ignore */ }
//
//  	protected JComponent createComponent( Action a )
//  	{
//  		return new JSeparator();
//  	}
//  }
}