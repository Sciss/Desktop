/*
 *  FileDialog.scala
 *  (Desktop)
 *
 *  Copyright (c) 2013-2014 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.desktop

import java.io.{FilenameFilter, File}
import java.awt
import scala.swing.FileChooser
import javax.swing.filechooser.FileFilter

object FileDialog {
  sealed trait Mode
  case object Open   extends Mode
  case object Save   extends Mode
  case object Folder extends Mode

  def apply(): FileDialog = new Impl()

  def save(init: Option[File] = None, title: String = "Save"): FileDialog = {
    val res   = apply()
    res.mode  = Save
    res.file  = init
    res.title = title
    res
  }

  def open(init: Option[File] = None, title: String = "Open"): FileDialog = {
    val res   = apply()
    res.mode  = Open
    res.file  = init
    res.title = title
    res
  }

  def folder(init: Option[File] = None, title: String = "Select Folder"): FileDialog = {
    val res   = apply()
    res.mode  = Folder
    res.file  = init
    res.title = title
    res
  }

  private final class Impl extends FileDialog {
    override def toString = s"FileDialog.${mode.toString.toLowerCase}@${hashCode().toHexString}"

    import FileChooser.SelectionMode.{DirectoriesOnly, FilesOnly}

    private var _owner: awt.Frame = null
    private var peerInit = false
    private lazy val peerIsAwt: Boolean = Desktop.isMac || mode != Folder // for folder, use JFileChooser on Windows + Linux

    lazy val peer: awt.FileDialog = {
      peerInit = true
      val res = new awt.FileDialog(_owner, _title, awtMode)
      if (_filter.isDefined) res.setFilenameFilter(fileNameFilter)
      if (_file  .isDefined) setAwtFile(res)
      res
    }

    private lazy val peerX: FileChooser = {
      peerInit  = true
      val res   = new FileChooser
      res.peer.setDialogType(awtMode)
      res.title = _title
      if (_filter.isDefined) res.fileFilter = fileFilter
      res.selectedFile = _file.orNull
      res.fileSelectionMode = if (_mode == Folder) DirectoriesOnly else FilesOnly
      res
    }

    private def fileNameFilter: FilenameFilter = _filter match {
      case Some(fun) => new FilenameFilter {
        def accept(dir: File, name: String): Boolean = fun(new File(dir, name))
      }
      case _ => null
    }

    private def fileFilter: FileFilter = _filter match {
      case Some(fun) => new FileFilter {
        def getDescription = "Filter" // what?

        def accept(f: File): Boolean = fun(f)
      }
      case _ => null
    }

    // note: compatible with JFileChooser (LOAD == OPEN_DIALOG, SAVE == SAVE_DIALOG)
    private def awtMode: Int = if (_mode == Save) awt.FileDialog.SAVE else awt.FileDialog.LOAD

    private var _mode: Mode = Open
    def mode = _mode
    def mode_=(value: Mode): Unit =
      if (_mode != value) {
        _mode = value
        if (peerInit) {
          if (peerIsAwt) {
            peer.setMode(awtMode)
          } else {
            peerX.peer.setDialogType(awtMode)
            peerX.fileSelectionMode = if (value == Folder) DirectoriesOnly else FilesOnly
          }
        }
      }

    private var _title = "Open"
    def title = _title
    def title_=(value: String): Unit =
      if (_title != value) {
        _title = value
        if (peerInit) {
          if (peerIsAwt)
            peer.setTitle(value)
          else
            peerX.title = value
        }
      }

    private var _filter = Option.empty[File => Boolean]
    def filter = _filter
    def filter_=(value: Option[File => Boolean]): Unit =
      if (_filter != value) {
        _filter = value
        if (peerInit) {
          if (peerIsAwt)
            peer.setFilenameFilter(fileNameFilter)
          else
            peerX.fileFilter = fileFilter
        }
      }

    // got to have sum fun!
    def setFilter(fun: (File) => Boolean): Unit = filter = Some(fun)

    private var _file = Option.empty[File]

    def file: Option[File] = {
      if (peerInit) {
        if (peerIsAwt) {
          val dir   = peer.getDirectory
          val file  = peer.getFile
          if (file != null)
            Some(new File(dir, file))
          else
            None
        } else {
          Option(peerX.selectedFile)
        }

      } else _file
    }

    def file_=(value: Option[File]): Unit =
      if (_file != value) {
        _file = value
        if (peerInit) {
          if (peerIsAwt)
            setAwtFile(peer)
          else
            peerX.selectedFile = value.orNull
        }
      }

    private def setAwtFile(dlg: awt.FileDialog): Unit = {
      val dir   = _file.map(_.getParent).orNull
      val name  = _file.map(_.getName  ).orNull
      dlg.setDirectory(dir)
      dlg.setFile(name)
    }

    def show(window: Option[Window]): Option[File] = if (peerIsAwt) showAwtDialog(window) else showJavaxDialog(window)

    private def showAwtDialog(window: Option[Window]): Option[File] = {
      if (!peerInit) _owner = window.map(Window.peer).orNull
      val dlg = peer
      if (mode == Folder) {
        val key       = "apple.awt.fileDialogForDirectories"
        val props     = sys.props
        val oldValue  = props.put(key, "true")
        try {
          peer.show() // "deprecated", but stupidly necessary, because `setVisible` does _not_ the same thing
        } finally {
          oldValue match {
            case Some(v)  => props.put(key, v)
            case _        => props.remove(key)
          }
        }
      } else {
        dlg.setVisible(true)
      }
      file
    }

    private def showJavaxDialog(window: Option[Window]): Option[File] = {
      val dlg   = peerX
      val code  = dlg.peer.showDialog(window.map(_.component.peer).orNull, null) // stupid Scala swing doesn't allow RootPanel
      val res   = FileChooser.Result(code)
      if (res == FileChooser.Result.Approve) Option(dlg.selectedFile) else None
    }
  }
}
sealed trait FileDialog extends DialogSource[Option[File]] {
  /** WARNING: this method is deprecated, as it might return an invalid
    * dialog on Windows and Linux when using `Folder` mode.
    */
  def peer: awt.FileDialog

  var mode  : FileDialog.Mode
  var file  : Option[File]
  var title : String
  var filter: Option[File => Boolean]

  /** Convenience method which will wrap the function in an `Option` and call `filter_=` */
  def setFilter(fun: File => Boolean): Unit
}