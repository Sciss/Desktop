package de.sciss.desktop

import java.io.File

import de.sciss.submin.Submin

import scala.swing.Swing

object FolderDialogTest extends App {
  Swing.onEDT {
    val isDark = args.contains("--dark")
    Submin.install(isDark)
    val dlg = FileDialog.folder(init = Some(new File(sys.props("user.home"))), title = "Folder Selection Test")
    val res = dlg.show(None)
    println(s"Result: $res / ${dlg.file}")
  }
}