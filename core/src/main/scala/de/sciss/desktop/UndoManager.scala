/*
 *  UndoManager.scala
 *  (Desktop)
 *
 *  Copyright (c) 2013-2014 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU Lesser General Public License v3+
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.desktop

import javax.swing.{undo => j}
import swing.Action

trait UndoManager {
  def peer: j.UndoManager

  def canUndo: Boolean
  def canRedo: Boolean
  def canUndoOrRedo: Boolean

  def significant: Boolean

  def undo(): Unit
  def redo(): Unit
  def undoOrRedo(): Unit

  def clear(): Unit
  def add(edit: j.UndoableEdit): Boolean

  def undoAction: Action
  def redoAction: Action
}