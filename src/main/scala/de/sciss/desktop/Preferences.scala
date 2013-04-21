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

import java.util.{prefs => j, StringTokenizer}
import impl.{PreferencesImpl => Impl}
import scala.util.control.NonFatal
import java.io.File
import java.awt.Dimension
import de.sciss.model.Model

object Preferences {

  def user  (clazz: Class[_]): Preferences = Impl.user  (clazz)
  def system(clazz: Class[_]): Preferences = Impl.system(clazz)

  type Update[A]    = Option[A]
  type Listener[A]  = Model.Listener[Update[A]]

  object Entry {
    def apply[A](prefs: Preferences, key: String)(implicit tpe: Preferences.Type[A]): Entry[A] =
      Impl.entry(prefs, key)

    def unapply[A](e: Entry[A]): Option[(Preferences, String)] = Some(e.preferences -> e.key)
  }
  trait Entry[A] extends Model[Update[A]] {
    def preferences: Preferences
    def key: String

    def get: Option[A]
    def getOrElse(default: => A): A
    def put(value: A): Unit
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

    implicit object files extends Type[List[File]] {
      def toString(value: List[File]) = value.map(_.getPath).mkString(File.pathSeparator)
      def valueOf(string: String): Option[List[File]] = {
        val tok = new StringTokenizer(string, File.pathSeparator)
        val b   = List.newBuilder[File]
        while (tok.hasMoreTokens) b += new File(tok.nextToken())
        Some(b.result())
      }
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