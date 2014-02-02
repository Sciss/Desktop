package de.sciss.desktop

import scala.swing.Swing
import java.io.File

object FolderDialogTest extends App {
  Swing.onEDT {
    val dlg = FileDialog.folder(init = Some(new File(sys.props("user.home"))), title = "Folder Selection Test")
    val res = dlg.show(None)
    println(s"Result: $res / ${dlg.file}")
  }
}