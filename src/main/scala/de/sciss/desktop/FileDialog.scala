/*
 *  FileDialog.scala
 *  (Desktop)
 *
 *  Copyright (c) 2013 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU General Public License
 *	as published by the Free Software Foundation; either
 *	version 2, june 1991 of the License, or (at your option) any later version.
 *
 *	This software is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *	General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public
 *	License (gpl.txt) along with this software; if not, write to the Free Software
 *	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.desktop

import java.io.{FilenameFilter, File}
import java.awt

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

    private var _owner: awt.Frame = null
    private var peerInit = false
    lazy val peer = {
      peerInit = true
      val res = new awt.FileDialog(_owner, title, awtMode)
      if (_filter.isDefined) res.setFilenameFilter(awtFilter)
      if (_file  .isDefined) setAwtFile(res)
      res
    }

    private def awtFilter = _filter match {
      case Some(fun) => new FilenameFilter {
        def accept(dir: File, name: String): Boolean = fun(new File(dir, name))
      }
      case _ => null
    }

    private def awtMode = if (_mode == Save) awt.FileDialog.SAVE else awt.FileDialog.LOAD

    private var _mode: Mode = Open
    def mode = _mode
    def mode_=(value: Mode): Unit =
      if (_mode != value) {
        _mode = value
        if (peerInit) peer.setMode(awtMode)
      }

    private var _title = "Open"
    def title = _title
    def title_=(value: String): Unit =
      if (_title != value) {
        _title = value
        if (peerInit) peer.setTitle(value)
      }

    private var _filter = Option.empty[File => Boolean]
    def filter = _filter
    def filter_=(value: Option[File => Boolean]): Unit =
      if (_filter != value) {
        _filter = value
        if (peerInit) peer.setFilenameFilter(awtFilter)
      }

    // gutta have sum fun!
    def setFilter(fun: (File) => Boolean): Unit = filter = Some(fun)

    private var _file = Option.empty[File]

    def file: Option[File] = {
      if (peerInit) {
        val dir   = peer.getDirectory
        val file  = peer.getFile
        if (file != null)
          Some(new File(dir, file))
        else
          None

      } else _file
    }

    private def setAwtFile(birne: awt.FileDialog): Unit = {
      val dir   = _file.map(_.getParent).orNull
      val name  = _file.map(_.getName  ).orNull
      birne.setDirectory(dir)
      birne.setFile(name)
    }

    def file_=(value: Option[File]): Unit =
      if (_file != value) {
        _file = value
        if (peerInit) setAwtFile(peer)
      }

    def show(window: Option[Window]): Option[File] = {
      if (!peerInit) _owner = window.map(Window.peer).orNull
      val dlg = peer
      if (mode == Folder) {
        val key       = "apple.awt.fileDialogForDirectories"
        val props     = sys.props
        val oldValue  = props.put(key, "true")
        try {
          peer.show()
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
  }
}
sealed trait FileDialog extends DialogSource[Option[File]] {
  def peer: awt.FileDialog

  var mode: FileDialog.Mode
  var file: Option[File]
  var title: String
  var filter: Option[File => Boolean]

  def setFilter(fun: File => Boolean): Unit
}