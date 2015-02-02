/*
 *  FileDialog.scala
 *  (Desktop)
 *
 *  Copyright (c) 2013-2015 Hanns Holger Rutz. All rights reserved.
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
import javax.swing.UIManager
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
    private var _multiple = false

    def multiple: Boolean = _multiple
    def multiple_=(value: Boolean): Unit =
      if (_multiple != value) {
        _multiple = value
        if (peerInit) {
          if (peerIsAwt) throw new UnsupportedOperationException("AWT cannot handle FileDialog.multiple = true")
          peerSwing.peer.setDialogType(awtMode)
          peerSwing.multiSelectionEnabled = value
        }
      }

    // JFileChooser is used for WebLaF. Otherwise, AWT is used except where not possible
    // (multiple selection, Folder on Windows and Linux)
    private lazy val peerIsAwt: Boolean = (UIManager.getLookAndFeel.getName != "WebLookAndFeel") && !multiple &&
      (Desktop.isMac || mode != Folder)

    private lazy val peerAwt: awt.FileDialog = {
      peerInit = true
      val res = new awt.FileDialog(_owner, _title, awtMode)
      if (_filter.isDefined) res.setFilenameFilter(fileNameFilter)
      if (_file  .isDefined) setAwtFile(res)
      res
    }

    private lazy val peerSwing: FileChooser = {
      peerInit  = true
      val res   = new FileChooser
      res.peer.setDialogType(awtMode)
      res.title = _title
      if (_filter.isDefined) res.fileFilter = fileFilter
      // Note: WebLaF doesn't handle `null` values!
      if (_file.isDefined) setSwingFile(res)
      res.fileSelectionMode     = if (_mode == Folder) DirectoriesOnly else FilesOnly
      res.multiSelectionEnabled = _multiple
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
            peerAwt.setMode(awtMode)
          } else {
            peerSwing.peer.setDialogType(awtMode)
            peerSwing.fileSelectionMode = if (value == Folder) DirectoriesOnly else FilesOnly
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
            peerAwt.setTitle(value)
          else
            peerSwing.title = value
        }
      }

    private var _filter = Option.empty[File => Boolean]
    def filter = _filter
    def filter_=(value: Option[File => Boolean]): Unit =
      if (_filter != value) {
        _filter = value
        if (peerInit) {
          if (peerIsAwt)
            peerAwt.setFilenameFilter(fileNameFilter)
          else
            peerSwing.fileFilter = fileFilter
        }
      }

    // got to have sum fun!
    def setFilter(fun: (File) => Boolean): Unit = filter = Some(fun)

    private var _file = Option.empty[File]

    def file: Option[File] = {
      if (peerInit) {
        if (peerIsAwt) {
          val dir   = peerAwt.getDirectory
          val file  = peerAwt.getFile
          if (file != null)
            Some(new File(dir, file))
          else
            None
        } else {
          Option(peerSwing.selectedFile)
        }

      } else _file
    }

    def file_=(value: Option[File]): Unit =
      if (_file != value) {
        _file = value
        if (peerInit) {
          if (peerIsAwt) {
            setAwtFile(peerAwt)
          } else {
            try {
              setSwingFile(peerSwing)
            } catch {
              case _: NullPointerException =>  // WebLaF has a bug currently, other LaFs support this idiom
            }
          }
        }
      }

    def files: List[File] = if (multiple && peerInit) peerSwing.selectedFiles.toList else file.toList

    private def setSwingFile(dlg: FileChooser): Unit = {
      val vn = _file.orNull
      if (vn != null && mode != Folder && vn.isDirectory) dlg.peer.setCurrentDirectory(vn)
      else dlg.selectedFile = vn
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
      val dlg = peerAwt
      if (mode == Folder) {
        val key       = "apple.awt.fileDialogForDirectories"
        val props     = sys.props
        val oldValue  = props.put(key, "true")
        try {
          peerAwt.show() // "deprecated", but stupidly necessary, because `setVisible` does _not_ the same thing
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
      val dlg   = peerSwing
      val code  = dlg.peer.showDialog(window.map(_.component.peer).orNull, null) // stupid Scala swing doesn't allow RootPanel
      val res   = FileChooser.Result(code)
      if (res == FileChooser.Result.Approve) Option(dlg.selectedFile) else None
    }
  }
}

/** A dialog for selecting a file or folder.
  * This tries to automatically select `java.awt.FileDialog` or `javax.swing.JFileChooser`
  * depending on the context. With many look-and-feels, the native (AWT) dialog provides a
  * better experience, for example on OS X. On the other hand, folder selection is not
  * officially supported by AWT, with OS X providing a tricky work-around.
  *
  * The new behaviour selected `JFileChooser` always, when the Web Look-and-Feel is
  * installed, as its component UI is quite sophisticated.
  */
trait FileDialog extends DialogSource[Option[File]] {
  /** Whether the dialog should allow the selection of an existing file
    * (`Open`), an existing folder (`Folder`) or a new file/folder (`Save`)
    */
  var mode  : FileDialog.Mode

  /** Whether it is possible to select multiple files. Defaults to `false`.
    * Since the `show` method returns an `Option[File]`, the correct procedure is
    * to check if the option is defined, and if so, to query `files`.
    */
  var multiple: Boolean

  var file  : Option[File]
  var title : String

  /** A predicate to filter the visible files in the dialog. Files are shown
    * for which the function returns `true`.
    */
  var filter: Option[File => Boolean]

  /** For single-selection mode, returns `file.toList`, for multiple-selection mode,
    * returns all selected files.
    */
  def files : List[File]

  /** Convenience method which will wrap the function in an `Option` and call `filter_=` */
  def setFilter(fun: File => Boolean): Unit
}