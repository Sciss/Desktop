/*
 *  PreferencesImpl.scala
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
      def preferenceChange(e: j.PreferenceChangeEvent): Unit =
        if (e.getKey == key) {
          val newValue = Option(e.getNewValue).flatMap(tpe.valueOf)
          dispatch(newValue)
        }
    }

    override protected def startListening(): Unit =
      preferences.peer.addPreferenceChangeListener(prefsListener)

    override protected def stopListening(): Unit =
      preferences.peer.removePreferenceChangeListener(prefsListener)

    def get: Option[A] = preferences.get(key)

    def getOrElse(default: => A): A = preferences.getOrElse(key, default)

    def put(value: A): Unit = preferences.put(key, value)

    def remove(): Unit = preferences.remove(key)
  }

  private final class Impl(val peer: j.Preferences, isSystem: Boolean, name: String) extends Preferences {
    import Preferences.Type

    override def toString = s"Preferences.${if (isSystem) "system" else "user"}($name)"

    def apply[A: Type](key: String): Preferences.Entry[A] = Preferences.Entry(this, key)

    def / (key: String): Preferences = new Impl(peer.node(key), isSystem = isSystem, name = s"$name.key")

    def get[A](key: String)(implicit tpe: Type[A]): Option[A] = {
      val s = peer.get(key, null)
      if (s == null) None else tpe.valueOf(s)
    }

    def getOrElse[A](key: String, default: => A)(implicit tpe: Type[A]): A = {
      val s = peer.get(key, null)
      if (s == null) default else tpe.valueOf(s).getOrElse(default)
    }

    def put[A](key: String, value: A)(implicit tpe: Type[A]): Unit =
      peer.put(key, tpe.toString(value))

    def remove(key: String): Unit = peer.remove(key)
  }
}