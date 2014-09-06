/*
 *  CompoundEdit.scala
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
package edit

import javax.swing.UIManager

object CompoundEdit {
  import javax.swing.undo.{CompoundEdit, UndoableEdit}

  /** Folds a sequence of edits. If the sequence size is one, the only element is simply returned.
    * If the sequence is empty, `None` is returned. Otherwise, a compound edit is returned.
    *
    * @param  seq   the sequence of edits to collapse
    * @param  name  the name to use for a compound edit. If the sequence size is less than or equal to one,
    *               this name is not used.
    */
  def apply(seq: List[UndoableEdit], name: String): Option[UndoableEdit] = seq match {
    case single :: Nil  => Some(single)
    case Nil            => None
    case several =>
      val ce = new Impl(name)
      several.foreach(ce.addEdit)
      ce.end()
      Some(ce)
  }

  private final class Impl(name: String) extends CompoundEdit {
    override def getPresentationName    : String = name
    override def getUndoPresentationName: String = {
      // yeah, crap. violating DRY because of
      // http://stackoverflow.com/questions/23598120/accessing-a-method-in-a-super-super-class
      // super[AbstractUndoableEdit].getUndoPresentationName
      val undoText = UIManager.getString("AbstractUndoableEdit.undoText")
      s"$undoText $name"
    }

    override def getRedoPresentationName: String = {
      val redoText = UIManager.getString("AbstractUndoableEdit.redoText")
      s"$redoText $name"
    }
  }
}
