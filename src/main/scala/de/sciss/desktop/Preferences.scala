/*
 *  Preferences.scala
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

import java.util.{prefs => j}
import impl.{PreferencesImpl => Impl}
import scala.util.control.NonFatal
import java.io.File
import java.awt.Dimension

object Preferences {

  def user  (clazz: Class[_]): Preferences = Impl.user  (clazz)
  def system(clazz: Class[_]): Preferences = Impl.system(clazz)

  type Listener[A] = Option[A] => Unit

  final case class Entry[A](prefs: Preferences, key: String)(implicit tpe: Type[A]) {
    private var listeners = Vector.empty[Listener[A]]

    private object prefsListener extends j.PreferenceChangeListener {
      def preferenceChange(e: j.PreferenceChangeEvent) {
        if (e.getKey == key) {
          val newValue = Option(e.getNewValue).flatMap(tpe.valueOf _)
          this.synchronized(listeners.foreach { l => try {
            l(newValue)
          } catch {
            case NonFatal(e1) => e1.printStackTrace()
          }})
        }
      }
    }

    def addListener(listener: Listener[A]) {
      prefsListener.synchronized {
        val add = listeners.isEmpty
        listeners :+= listener
        if (add) prefs.peer.addPreferenceChangeListener(prefsListener)
      }
    }

    def removeListener(listener: Listener[A]) {
      prefsListener.synchronized {
        val i = listeners.indexOf(listener)
        if (i >= 0) {
          listeners = listeners.patch(0, Vector.empty, 1)
          val remove = listeners.isEmpty
          if (remove) prefs.peer.removePreferenceChangeListener(prefsListener)
        }
      }
    }

    def get: Option[A] = prefs.get(key)
    def getOrElse(default: => A): A = prefs.getOrElse(key, default)
    def put(value: A) { prefs.put(key, value) }
  }

  object Type {
    implicit object string extends Type[String] {
      def toString(value: String) = value
      def valueOf(string: String): Option[String] = Some(string)
    }

    implicit object file extends Type[File] {
      def toString(value: File) = value.getPath
      def valueOf(string: String): Option[File] = Some(new File(string))
    }

    implicit object int extends Type[Int] {
      def toString(value: Int) = value.toString
      def valueOf(string: String): Option[Int] = try {
        Some(string.toInt)
      } catch {
        case NonFatal(_) => None
      }
    }

    implicit object boolean extends Type[Boolean] {
      def toString(value: Boolean) = value.toString
      def valueOf(string: String): Option[Boolean] = try {
        Some(string.toBoolean)
      } catch {
        case NonFatal(_) => None
      }
    }

    implicit object dimension extends Type[Dimension] {
      def toString(value: Dimension): String = s"${value.width} ${value.height}"
      def valueOf(string: String): Option[Dimension] = try {
        val i = string.indexOf(' ')
        if (i < 0) return None
        val width   = string.substring(0, i).toInt
        val height  = string.substring(i +1).toInt
        Some(new Dimension(width, height))
      }
      catch {
        case NonFatal(_) => None
      }
    }
  }
  trait Type[A] {
    def toString(value: A): String
    def valueOf(string: String): Option[A]
  }
}
trait Preferences {
  import Preferences.Type

  private[desktop] def peer: j.Preferences

  def get[A: Type](key: String): Option[A]
  def getOrElse[A: Type](key: String, default: => A): A
  def put[A: Type](key: String, value: A): Unit
  def / (key: String): Preferences

  def apply[A: Type](key: String): Preferences.Entry[A]
}