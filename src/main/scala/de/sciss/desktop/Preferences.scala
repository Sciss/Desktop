package de.sciss.desktop

import java.util.{prefs => j}
import impl.{PreferencesImpl => Impl}
import scala.util.control.NonFatal

object Preferences {
  object Type {
    import java.lang.{String => SString}

    implicit object String extends Type[SString] {
      private[desktop] def toString(value: SString) = value
      private[desktop] def valueOf(string: SString): Option[SString] = Some(string)
    }

    implicit object File extends Type[java.io.File] {
      private[desktop] def toString(value: java.io.File) = value.getPath
      private[desktop] def valueOf(string: SString): Option[java.io.File] = Some(new java.io.File(string))
    }

    implicit object Int extends Type[scala.Int] {
      private[desktop] def toString(value: scala.Int) = value.toString
      private[desktop] def valueOf(string: SString): Option[scala.Int] = try {
        Some(string.toInt)
      } catch {
        case NonFatal(_) => None
      }
    }

    implicit object Boolean extends Type[scala.Boolean] {
      private[desktop] def toString(value: scala.Boolean) = value.toString
      private[desktop] def valueOf(string: SString): Option[scala.Boolean] = try {
        Some(string.toBoolean)
      } catch {
        case NonFatal(_) => None
      }
    }

    implicit object Dimension extends Preferences.Type[java.awt.Dimension] {
      private[desktop] def toString(value: java.awt.Dimension): String = s"${value.width} ${value.height}"
      private[desktop] def valueOf(string: String): Option[java.awt.Dimension] = try {
        val i = string.indexOf(' ')
        if (i < 0) return None
        val width   = string.substring(0, i).toInt
        val height  = string.substring(i +1).toInt
        Some(new java.awt.Dimension(width, height))
      }
      catch {
        case NonFatal(_) => None
      }
    }
  }
  trait Type[A] {
    private[desktop] def toString(value: A): String
    private[desktop] def valueOf(string: String): Option[A]

//    private[desktop] def put(prefs: j.Preferences, key: String, value: A): Unit
//    private[desktop] def get(prefs: j.Preferences, key: String): Option[A]
//    private[desktop] def getOrElse(prefs: j.Preferences, key: String, default: A): A
  }

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