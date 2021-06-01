/*
 *  LogPane.scala
 *  (Desktop)
 *
 *  Copyright (c) 2013-2021 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.desktop

import java.io.{OutputStream, Writer}

import scala.swing.{Color, Component, Font}

object LogPane {
  def apply(rows: Int = 10, columns: Int = 60): LogPane = new impl.LogPaneImpl(rows, columns)
}

/** A pane widget which can be used to log text output, and which can be hooked up to capture the
  * default console output.
  */
trait LogPane {
  /** The Swing component which can be added to a Swing parent container. */
  def component: Component

  var font: Font
  var foreground: Color
  var background: Color

  var lineWrap: Boolean

  var rows: Int
  var columns: Int

  /** A `Writer` which will write to the pane. */
  def writer: Writer

  /** An `OutputStream` which will write to the pane. */
  def outputStream: OutputStream

  /**  Clears the contents of the pane. */
  def clear(): Unit

  /** Makes this log pane the default text output for
    * `System.out` and optionally for `System.err` as well.
    *
    * @return  the method returns the log pane itself for convenience and method concatenation
    */
  def makeDefault(error: Boolean = true): this.type
}