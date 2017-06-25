/*
 *  UndoManager.scala
 *  (Desktop)
 *
 *  Copyright (c) 2013-2017 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.desktop

import javax.swing.{undo => j}

import de.sciss.desktop.impl.UndoManagerImpl

import swing.Action

object UndoManager {
  def apply(): UndoManager = new UndoManagerImpl
}
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