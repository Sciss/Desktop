/*
 *  RecentFilesImpl.scala
 *  (Desktop)
 *
 *  Copyright (c) 2013-2018 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.desktop
package impl

import java.awt.EventQueue
import javax.swing.{JFileChooser, KeyStroke}

import de.sciss.file.File

import scala.collection.immutable.{IndexedSeq => Vec}
import scala.swing.{Action, Swing}

object RecentFilesImpl {
  private val dummyFile = new File("")

  private def fork(code: => Unit): Unit =
    if (EventQueue.isDispatchThread) code else Swing.onEDT(code)
}
class RecentFilesImpl(entry: Preferences.Entry[List[File]], maxItems: Int, action: File => Unit,
                         keyStroke: Option[KeyStroke])
  extends RecentFiles {

  import de.sciss.desktop.impl.RecentFilesImpl._

  private final class FileAction extends Action("file") {
    var file: File = dummyFile

    def apply(): Unit =
      if (file != dummyFile) action(file)
  }

  private[this] val actions: Vec[FileAction] = Vec.fill(maxItems)(new FileAction)
  actions(0).accelerator = keyStroke

  // cf http://nadeausoftware.com/node/89

  private[this] lazy val chooser = new JFileChooser()

  private[this] val l = entry.addListener {
    case Some(entries) => fork(updateEntries(entries))
  }

  private[this] val items = actions.zipWithIndex.map { case (ac, i) =>
    val it      = Menu.Item("file" + i, ac)
    it.visible  = false
    it
  }

  private[this] val group = Menu.Group("openrecent", "Open Recent")

  val menu: Menu.Group = {
    items.foreach(group add _)
    group.addLine()
    group.add(Menu.Item("clear")("Clear Menu") {
      entry.put(Nil)
    })
    group
  }

  fork(updateEntries(files))

  // runs on EDT!
  private def updateEntries(entries: List[File]): Unit = {
    val v   = entries.toVector
    // val fsv = FileSystemView.getFileSystemView

    for (i <- 0 until maxItems) {
      val ac = actions(i)
      val it = items(i)
      if (i < v.size) {
        val file    = v(i)
        ac.file     = file
        ac.enabled  = file.canRead
        val name    = file.getName
        val multi   = v.count(_.getName == name) > 1
        ac.icon     = chooser.getIcon(file) // fsv.getSystemIcon(file)
        // if there is more than one file with the same name, append parent folder.
        // TODO: while this is how OS X does it, it would be smarter to look for
        // different components of the parent folder, because we can still end up
        // with two identical entries this way. Or add a tool tip?
        ac.title    = if (multi) s"$name — ${file.getParentFile.getName}" else name
        it.visible  = true

      } else {
        if (it.visible) {
          it.visible  = false
          ac.file     = dummyFile
          ac.icon     = null
          ac.title    = "file"
        }
      }
    }

    group.enabled = v.nonEmpty
  }

  def add(file: File): Unit = {
    val f0  = files
    if (f0.headOption.contains(file)) return // already at top
    val f1  = f0.filterNot(_ == file)
    val f2  = file :: f1
    val f3  = if (f2.lengthCompare(maxItems) > 0) f2.take(maxItems) else f2
    entry.put(f3)
  }

  def remove(file: File): Unit = {
    val f0  = files
    if (!f0.contains(file)) return
    val f1  = f0.filterNot(_ == file)
    entry.put(f1)
  }

  def files: List[File] = entry.getOrElse(Nil)

  def dispose(): Unit = entry.removeListener(l)
}
