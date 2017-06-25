package de.sciss.desktop

object LinuxRevealFileTest {
  def main(args: Array[String]): Unit = {
    import de.sciss.file._
    (userHome / "Documents").children(_.isFile).headOption.fold[Unit] {
      println("No file in ~/Documents. Aborting.")
    } { f =>
      println(s"Revealing $f")
      Desktop.revealFile(f)
    }
  }
}
