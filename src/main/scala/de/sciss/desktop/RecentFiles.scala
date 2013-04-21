package de.sciss.desktop

import java.io.File
import scala.swing.{Action, Swing}

object RecentFiles {
  def apply(entry: Preferences.Entry[List[File]], maxItems: Int = 10): RecentFiles =
    new Impl(entry, maxItems)

  private final class Impl(entry: Preferences.Entry[List[File]], maxItems: Int) extends RecentFiles {
    private val sync = new AnyRef

    private var menuInit = false

    private val l = entry.addListener {
      case Some(entries)  =>
        sync.synchronized {
          if (menuInit) Swing.onEDT(updateEntries(entries))
        }
    }

    // runs on EDT!
    private def updateEntries(entries: List[File]) {
      sync.synchronized {

      }
    }

    lazy val menu: Menu.Group = {
      import Menu._
      sync.synchronized {
        val res = Group("openrecent", "Open Recent")
        val ac1 = new Action("file1") {
          def apply() {
            ???
          }
        }
        val it1 = Item("file1", ac1)
        // it1.visible = false

        menuInit = true
        res
      }
    }

    def add(file: File) {
      val f0  = files
      if (f0.headOption == Some(file)) return // already at top
      val f1  = f0.filterNot(_ == file)
      val f2  = file :: f1
      val f3  = if (f2.size > maxItems) f2.take(maxItems) else f2
      entry.put(f3)
    }

    def remove(file: File) {
      val f0  = files
      if (!f0.contains(file)) return
      val f1  = f0.filterNot(_ == file)
      entry.put(f1)
    }

    def files: List[File] = entry.getOrElse(Nil)

    def dispose() {
      entry.removeListener(l)
    }
  }
}
trait RecentFiles {
  def menu: Menu.Group
  def add(file: File): Unit
  def remove(file: File): Unit
  def files: List[File]

  def dispose(): Unit
}