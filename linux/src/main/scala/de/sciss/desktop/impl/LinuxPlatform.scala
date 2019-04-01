/*
 *  LinuxPlatform.scala
 *  (Desktop)
 *
 *  Copyright (c) 2013-2019 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.desktop
package impl

import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.{Date, Locale}

import de.sciss.desktop.Desktop.Update
import de.sciss.file._
import de.sciss.model.Model

import scala.annotation.tailrec
import scala.concurrent.Future
import scala.swing.Image

object LinuxPlatform extends Platform {
  override def toString = "LinuxPlatform"

  private[this] val execDirs = Array[String]("/usr/local/bin", "/usr/bin")

  private def findExec(fileName: String): Option[File] = {
    var i = 0
    val folders = execDirs
    while (i < folders.length) {
      val folder = folders(i)
      val f = new File(folder, fileName)
      if (f.canExecute) return Some(f)
      i += 1
    }
    None
  }

  def revealFile(file: File): Unit = {
    val fileAbs = file.absolute
    import sys.process._
    findExec("nautilus") match {
      case Some(cmd) => Seq(cmd.path, fileAbs.path).!
      case None =>
        for {
          cmd     <- findExec("xdg-open")
          parent  <- fileAbs.parentOption
        } {
          Seq(cmd.path, parent.path).!
        }
    }
  }

  private def xdgDataHome(): File = sys.env.get("XDG_DATA_HOME").fold(userHome / ".local" / "share")(file)

  private def createUniqueFile(parent: File, base: String, ext: String): File = {
    val ext1 = if (ext.isEmpty || ext.charAt(0) == '.') ext else s".$ext"
    @tailrec def loop(count: Int): File = {
      val name  = s"$base-$count$ext1"
      val test  = parent / name
      if (!test.exists()) test else loop(count + 1)
    }
    loop(1)
  }

  private def escapedPath(f: File): String = {
    val u = f.toURI.toString
    assert(u.startsWith("file:"))
    u.substring(5)
  }

  def moveFileToTrash(file: File): Unit = {
    // cf. http://standards.freedesktop.org/trash-spec/trashspec-1.0.html
    val trash = xdgDataHome() / "Trash"
    if (!trash.exists()) trash.mkdirs()
    val files = trash / "files"
    if (!files.exists()) files.mkdir()
    val target0 = files / file.name

    val targetFile = if (!target0.exists()) target0 else {
      val (base, ext) = file.baseAndExt
      createUniqueFile(files, base = base, ext = ext)
    }
    val info = trash / "info"
    if (!info.exists()) info.mkdir()
    val infoFile = info / s"${targetFile.name}.trashinfo"
    // example content:
    //
    // [Trash Info]
    // Path=foo/bar/meow.bow-wow
    // DeletionDate=20040831T22:32:08

    import sys.process._
    val mvRes = Seq("mv", file.absolutePath, targetFile.path).!
    // println(s"mv result: $mvRes")
    if (mvRes == 0) {
      val sourceEscape  = escapedPath(file.absolute)
      val deletionDate  = new SimpleDateFormat("YYYY-MM-DD'T'hh:mm:ss", Locale.US).format(new Date())
      val infoString =
        s"""[Trash Info]
           |Path=$sourceEscape
           |DeletionDate=$deletionDate
           |""".stripMargin
      val infoS = new FileOutputStream(infoFile)
      infoS.write(infoString.getBytes("UTF-8"))
      infoS.close()
    }
  }

  def addListener   (pf: Model.Listener[Update]): pf.type = pf
  def removeListener(pf: Model.Listener[Update]): Unit    = ()

  def setDockImage(image: Image         ): Unit = ()
  def setDockBadge(label: Option[String]): Unit = ()

  def requestUserAttention (repeat    : Boolean): Unit = ()
  def requestForeground    (allWindows: Boolean): Unit = ()

  def setQuitHandler       (test  : => Future[Unit]): Boolean = false

  def setAboutHandler      (action: => Unit): Boolean = false
  def setPreferencesHandler(action: => Unit): Boolean = false
}