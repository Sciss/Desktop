/*
 *  PreferencesImpl.scala
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

import java.util.{prefs => j}
import de.sciss.model.impl.ModelImpl

object PreferencesImpl {
  def user(clazz: Class[_]): Preferences =
    new Impl(j.Preferences.userNodeForPackage(clazz), isSystem = false, clazz.getName)

  def system(clazz: Class[_]): Preferences =
    new Impl(j.Preferences.systemNodeForPackage(clazz), isSystem = true, clazz.getName)

  def entry[A](prefs: Preferences, key: String)(implicit tpe: Preferences.Type[A]): Preferences.Entry[A] =
    new EntryImpl(prefs, key)

  private final class EntryImpl[A](val preferences: Preferences, val key: String)
                           (implicit tpe: Preferences.Type[A])
    extends Preferences.Entry[A] with ModelImpl[Preferences.Update[A]] {

    private type Listener = Preferences.Listener[A]

    private object prefsListener extends j.PreferenceChangeListener {
      def preferenceChange(e: j.PreferenceChangeEvent) {
        if (e.getKey == key) {
          val newValue = Option(e.getNewValue).flatMap(tpe.valueOf _)
          dispatch(newValue)
        }
      }
    }

    override protected def startListening() {
      preferences.peer.addPreferenceChangeListener(prefsListener)
    }

    override protected def stopListening() {
      preferences.peer.removePreferenceChangeListener(prefsListener)
    }

    def get: Option[A] = preferences.get(key)
    def getOrElse(default: => A): A = preferences.getOrElse(key, default)
    def put(value: A) { preferences.put(key, value) }
  }

  private final class Impl(val peer: j.Preferences, isSystem: Boolean, name: String) extends Preferences {
    import Preferences.Type

    override def toString = s"Preferences.${if (isSystem) "system" else "user"}($name)"

    def apply[A: Type](key: String) = Preferences.Entry[A](this, key)

    def / (key: String): Preferences = new Impl(peer.node(key), isSystem = isSystem, name = s"$name.key")

    def get[A](key: String)(implicit tpe: Type[A]): Option[A] = {
      val s = peer.get(key, null)
      if (s == null) None else tpe.valueOf(s)
    }

    def getOrElse[A](key: String, default: => A)(implicit tpe: Type[A]): A = {
      val s = peer.get(key, null)
      if (s == null) default else tpe.valueOf(s).getOrElse(default)
    }

    def put[A](key: String, value: A)(implicit tpe: Type[A]) {
      peer.put(key, tpe.toString(value))
    }
  }
}