/*
 *  PathButton.scala
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

import java.awt.datatransfer.{DataFlavor, Transferable, UnsupportedFlavorException}
import java.awt.event.MouseEvent
import java.io.{File, IOException}
import java.util

import javax.swing.event.MouseInputAdapter
import javax.swing.{JComponent, TransferHandler}

import scala.swing.event.ValueChanged
import scala.swing.{Action, Button}

object PathButton {
  private val supportedFlavors = Array(DataFlavor.javaFileListFlavor, DataFlavor.stringFlavor)
}
class PathButton extends Button(null: String) {
  import PathButton._

  private[this] var _value: File    = new File("")
  private[this] var _title: String  = _

  /** The default mode is `Open`. */
  var mode: FileDialog.Mode = FileDialog.Open

  def title: String =
    if (_title != null) _title else mode match {
      case FileDialog.Open    => "Open File"
      case FileDialog.Save    => "Save File"
      case FileDialog.Folder  => "Choose Folder"
    }

  def title_=(value: String): Unit = _title = value

  var accept: File => Option[File] = Some(_)

  def value: File = _value

  def value_=(f: File): Unit = if (_value != f) {
    _value = f
    publish(new ValueChanged(this))
  }

  /** Treats empty file as `None` */
  def valueOption: Option[File] = if (_value.getPath == "") None else Some(_value)
  def valueOption_=(opt: Option[File]): Unit = value = opt.getOrElse(new File(""))

  // ---- constructor ----

  {
    val p = peer
    p.setTransferHandler(TransferHandlerImpl)
    p.addMouseListener      (MouseImpl)
    p.addMouseMotionListener(MouseImpl)

    val a = Action("…") {
      showFileChooser()
    }
    a.toolTip = "Show File Chooser"
    action = a
  }

  // ---- private ----

  private def showFileChooser(): Unit = {
    val dlg   = FileDialog()
    dlg.mode  = mode
    dlg.file  = valueOption
    dlg.title = title
    dlg.show(Window.find(this)).flatMap(accept).foreach(value = _)
  }

  private object MouseImpl extends MouseInputAdapter {
    private[this] var dndInit: MouseEvent = _
    private[this] var dndStarted          = false

    override def mousePressed(e: MouseEvent): Unit = {
      dndInit     = e
      dndStarted  = false
    }

    override def mouseReleased(e: MouseEvent): Unit = {
      // if (!dndStarted && peer.contains(e.getPoint)) showFileChooser()
      dndInit     = null
      dndStarted  = false
    }

    override def mouseDragged(e: MouseEvent): Unit = {
      if (!dndStarted && (dndInit != null) &&
        ((Math.abs(e.getX - dndInit.getX) > 5) || (Math.abs(e.getY - dndInit.getY) > 5))) {

        val c = e.getSource.asInstanceOf[JComponent]
        c.getTransferHandler.exportAsDrag(c, e, TransferHandler.COPY)
        dndStarted = true
        val m = peer.getModel
        m.setArmed  (false)
        m.setPressed(false)
      }
    }
  }

  private object TransferHandlerImpl extends TransferHandler {
    /* Import a path (file list or string) if it is available. */
    override def importData(c: JComponent, t: Transferable): Boolean = {
      try {
        val newPath = if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
          val td = t.getTransferData(DataFlavor.javaFileListFlavor)
          td match {
            case fileList: util.List[_] =>
              if (fileList.isEmpty) null else {
                val elem = fileList.get(0)
                elem match {
                  case f: File  => f
                  case _        => new File(elem.toString)
                }
              }

            case _ => null
          }

        } else if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
          new File(t.getTransferData(DataFlavor.stringFlavor).asInstanceOf[String])
        } else {
          null
        }
        (newPath != null) && {
          value = newPath
          true
        }

      } catch {
        case e1: UnsupportedFlavorException => e1.printStackTrace(); false
        case e2: IOException                => e2.printStackTrace(); false
      }
    }

    override def getSourceActions(c: JComponent): Int = TransferHandler.COPY

    override protected def createTransferable(c: JComponent): Transferable =
      valueOption.map(f => new PathTransferable(f)).orNull

    override def canImport(c: JComponent, flavors: Array[DataFlavor]): Boolean =
      flavors.exists(supportedFlavors.contains)
  }

  private class PathTransferable(val f: File) extends Transferable {
    override def getTransferDataFlavors: Array[DataFlavor] = supportedFlavors

    override def isDataFlavorSupported(flavor: DataFlavor): Boolean =
      supportedFlavors.contains(flavor)

    override def getTransferData(flavor: DataFlavor): AnyRef = {
      if (f == null) {
        throw new IOException
      } else if (flavor == DataFlavor.javaFileListFlavor) {
        val coll = new util.ArrayList[File](1)
        coll.add(f)
        coll
      } else if (flavor == DataFlavor.stringFlavor) {
        f.getAbsolutePath
      } else {
        throw new UnsupportedFlavorException(flavor)
      }
    }
  }
}
