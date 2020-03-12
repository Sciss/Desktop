/*
 *  Preferences.scala
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

import java.awt.Dimension
import java.util.{StringTokenizer, prefs => j}

import de.sciss.desktop.impl.{PreferencesImpl => Impl}
import de.sciss.file.File
import de.sciss.model.Model

import scala.util.control.NonFatal

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
    def remove(): Unit
  }

  object Type {
    implicit object string extends Type[String] {
      def toString(value: String): String = value
      def valueOf(string: String): Option[String] = Some(string)
    }

    implicit object file extends Type[File] {
      def toString(value: File): String = value.getPath
      def valueOf(string: String): Option[File] = Some(new File(string))
    }

    implicit object files extends Type[List[File]] {
      def toString(value: List[File]): String = value.map(_.getPath).mkString(File.pathSep.toString)
      def valueOf(string: String): Option[List[File]] = {
        val tok = new StringTokenizer(string, File.pathSep.toString)
        val b   = List.newBuilder[File]
        while (tok.hasMoreTokens) b += new File(tok.nextToken())
        Some(b.result())
      }
    }

    implicit object int extends Type[Int] {
      def toString(value: Int): String = value.toString
      def valueOf(string: String): Option[Int] = try {
        Some(string.toInt)
      } catch {
        case NonFatal(_) => None
      }
    }

    implicit object boolean extends Type[Boolean] {
      def toString(value: Boolean): String = value.toString
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
  import de.sciss.desktop.Preferences.Type

  private[desktop] def peer: j.Preferences

  def get[A: Type](key: String): Option[A]
  def getOrElse[A: Type](key: String, default: => A): A
  def put[A: Type](key: String, value: A): Unit
  def / (key: String): Preferences

  def remove(key: String): Unit

  def apply[A: Type](key: String): Preferences.Entry[A]
}