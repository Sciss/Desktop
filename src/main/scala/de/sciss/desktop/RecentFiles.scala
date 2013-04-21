package de.sciss.desktop

import java.io.File
import scala.swing.{Action, Swing}
import java.awt.EventQueue
import javax.swing.{KeyStroke, JFileChooser}
import java.awt.event.{InputEvent, KeyEvent}

object RecentFiles {
  private lazy val _defaultKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_O, Window.menuShortcut + InputEvent.ALT_MASK)

  def defaultKeyStroke = _defaultKeyStroke

  def apply(entry: Preferences.Entry[List[File]], maxItems: Int = 10,
            keyStroke: Option[KeyStroke] = Some(defaultKeyStroke))
           (action: File => Unit): RecentFiles =
    new Impl(entry, maxItems, action, keyStroke)

  private val dummyFile = new File("")

  private def fork(code: => Unit) {
    if (EventQueue.isDispatchThread) code else Swing.onEDT(code)
  }

  private final class Impl(entry: Preferences.Entry[List[File]], maxItems: Int, action: File => Unit,
                           keyStroke: Option[KeyStroke])
    extends RecentFiles {

    private final class FileAction extends Action("file") {
      var file = dummyFile

      def apply() {
        if (file != dummyFile) action(file)
      }
    }

    private val actions = Vector.fill(maxItems)(new FileAction)
    actions(0).accelerator = keyStroke

    // cf http://nadeausoftware.com/node/89

    private val chooser = new JFileChooser()

    private val l = entry.addListener {
      case Some(entries) => fork(updateEntries(entries))
    }

    // runs on EDT!
    private def updateEntries(entries: List[File]) {
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

    private val items = actions.zipWithIndex.map { case (ac, i) =>
      val it      = Menu.Item("file" + i, ac)
      it.visible  = false
      it
    }

    private val group = Menu.Group("openrecent", "Open Recent")

    val menu: Menu.Group = {
      items.foreach(group add _)
      group.addLine()
      group.add(Menu.Item("clear")("Clear Menu") {
        entry.put(Nil)
      })
      group
    }

    fork(updateEntries(files))

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