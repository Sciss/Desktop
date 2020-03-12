/*
 *  PrefsGUI.scala
 *  (Desktop)
 *
 *  Copyright (c) 2013-2020 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.desktop

import javax.swing.SpinnerNumberModel

import de.sciss.file._
import de.sciss.swingplus
import de.sciss.swingplus.{ComboBox, Spinner}

import scala.swing.Swing.EmptyIcon
import scala.swing.event.{ButtonClicked, EditDone, SelectionChanged, ValueChanged}
import scala.swing.{Alignment, CheckBox, Component, Label, TextField}

object PrefsGUI {
  def label(text: String) = new Label(text + ":", EmptyIcon, Alignment.Right)

  def intField(prefs: Preferences.Entry[Int], default: => Int, min: Int = 0, max: Int = 65536,
               step: Int = 1): Component = {
    val m  = new SpinnerNumberModel(prefs.getOrElse(default), min, max, step)
    val gg = new Spinner(m)
    gg.listenTo(gg)
    gg.reactions += {
      case ValueChanged(_) => gg.value match{
        case i: Int => prefs.put(i)
        case _ => println(s"Unexpected value ${gg.value}")
      }
    }
    gg.tooltip = s"Default: $default"
    gg
  }

  def pathField(prefs: Preferences.Entry[File], default: => File, title: String,
                accept: File => Option[File] = Some(_)): Component =
    pathField1(prefs, default, title, accept)

  // XXX TODO -- should fuse with `pathField` in next major version
  def pathField1(prefs: Preferences.Entry[File], default: => File, title: String,
                 accept: File => Option[File] = Some(_), mode: FileDialog.Mode = FileDialog.Open): Component = {
    def fixDefault: File = default  // bug in Scala 2.10
    val gg = new PathField
    gg.title  = title
    gg.accept = accept
    gg.mode   = mode
    gg.value  = prefs.getOrElse(fixDefault)
    gg.reactions += {
      case ValueChanged(_) =>
        val f = gg.valueOption.getOrElse(fixDefault)
        if (gg.valueOption.isEmpty) gg.value = fixDefault
        prefs.put(f)
    }
    gg
  }

  def textField(prefs: Preferences.Entry[String], default: => String): Component = {
    def fixDefault: String = default  // XXX TODO: Scalac bug?
    val gg = new TextField(prefs.getOrElse(default), 16)
    gg.listenTo(gg)
    gg.reactions += {
      case EditDone(_) =>
        if (gg.text.isEmpty) gg.text = fixDefault
        prefs.put(gg.text)
    }
    // gg.tooltip = s"Default: $default"
    gg
  }

  def combo[A](prefs: Preferences.Entry[A], default: => A, values: Seq[A])(implicit view: A => String): Component = {
    val gg      = new ComboBox[A](values)
    gg.renderer = swingplus.ListView.Renderer(view)(gg.renderer.asInstanceOf[swingplus.ListView.Renderer[String]])
    // gg.peer.putClientProperty("JComboBox.isSquare", true)
    val idx0 = values.indexOf(prefs.getOrElse(default))
    if (idx0 >= 0) gg.selection.index = idx0
    gg.listenTo(gg.selection)
    gg.reactions += {
      case SelectionChanged(_) =>
        val it = gg.selection.item
        // println(s"put($it)")
        prefs.put(it)
    }
    gg.maximumSize = gg.preferredSize // avoid excessive width
    gg
  }

  def checkBox(prefs: Preferences.Entry[Boolean], default: => Boolean): Component = {
    val gg = new CheckBox
    val sel0 = prefs.getOrElse(default)
    gg.selected = sel0
    gg.listenTo(gg)
    gg.reactions += {
      case ButtonClicked(_) =>
        val sel1 = gg.selected
        prefs.put(sel1)
    }
    gg
  }
}