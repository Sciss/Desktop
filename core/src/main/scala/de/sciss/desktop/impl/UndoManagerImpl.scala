/*
 *  UndoManagerImpl.scala
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
package impl

import javax.{swing => j}
import javax.swing.undo.{UndoableEdit, CannotRedoException, CannotUndoException, CompoundEdit}
import swing.Action
import javax.swing.KeyStroke
import java.awt.event.{InputEvent, KeyEvent}
import de.sciss.desktop.Window

trait UndoManagerImpl extends UndoManager {
  manager =>

  protected var dirty: Boolean

	/*
	 *	The concept of pendingEdits is that
	 *	insignificant (visual only) edits
	 *	should not destroy the redo tree
	 *	(which they do in the standard undo manager).
	 *	Imagine the user places a marker on the timeline,
	 *	hits undo, then accidentally or by intention
	 *	moves the timeline position. This will render
	 *	the redo of the marker add impossible. Now the
	 *	timeline position is insignificant and hence
	 *	will be placed on the pendingEdits stack.
	 *	This is a &quot;lazy&quot; thing because these
	 *	pendingEdits will only be added to the real
	 *	undo history when the next significant edit comes in.
	 */
	private var pendingEditCount  = 0  // have to count separately, because CompoundEdit hasn't got getter methods
  private var pendingEdits      = new CompoundEdit()

  object peer extends j.undo.UndoManager {
    override def redo(): Unit =
  		try {
  			undoPending()
  			super.redo()
  		}
  		finally {
  			updateStates()
  		}

  	override def undo(): Unit =
  		try {
  			undoPending()
  			super.undo()
  		}
  		finally {
  			updateStates()
  		}

    override def editToBeUndone: UndoableEdit = super.editToBeUndone
    override def editToBeRedone: UndoableEdit = super.editToBeRedone

    def _edits = edits  // widening visibility

    override def addEdit(anEdit: UndoableEdit): Boolean = {
      if (anEdit.isSignificant) {
        pendingEdits.synchronized {
          if (pendingEditCount > 0) {
            pendingEdits.end()
            super.addEdit(pendingEdits)
            pendingEdits = new CompoundEdit()
            pendingEditCount = 0
          }
        }
        val result = super.addEdit(anEdit)
        updateStates()
        result
      } else {
        pendingEdits.synchronized {
          pendingEditCount += 1
          pendingEdits.addEdit(anEdit)
        }
      }
    }

    override def discardAllEdits(): Unit = {
      pendingEdits.synchronized {
   			pendingEdits.die()
   			pendingEdits = new CompoundEdit()
   			pendingEditCount = 0
   		}
   		super.discardAllEdits()
   		updateStates()
   	}
  }

  peer.setLimit(1000)
  updateStates()

  //	/**
  //	 *  Get an Action object that will undo the
  //	 *	last step in the undo history.
  //	 *
  //	 *  @return <code>Action</code> suitable for attaching to a <code>JMenuItem</code>.
  //	 */
  //	def getUndoAction: j.Action = ActionUndo.peer

  //	/**
  //	 *  Get an Action object that will redo the
  //	 *	next step in the undo history.
  //	 *
  //	 *  @return <code>Action</code> suitable for attaching to a <code>JMenuItem</code>.
  //	 */
  //	def getRedoAction: j.Action = _redoAction.peer

  /** Gets an `Action` object that will dump the
    * current undo history to the console.
    *
    * @return action suitable for attaching to a menu item
	  */
	final def debugDumpAction: Action = ActionDebugDump

	/** Adds a new edit to the undo history.
	  * This behaves just like the normal
	  * UndoManager, i.e. it tries to replace
	  * the previous edit if possible. When
	  * the edits `isSignificant()`
	  * method returns true, the main application
	  * is informed about this edit by calling
	  * the `setModified` method.
	  * Also the undo and redo action's enabled
	  * / disabled states are updated.
	  *
	  *	Insignificant edits are saved in a pending
	  *	compound edit that gets added with the
	  *	next significant edit to allow redo as
	  *	long as possible.
	  *
	  * @see	javax.swing.undo.UndoableEdit#isSignificant()
	  * @see	javax.swing.Action#setEnabled( boolean )
	  */

	private def undoPending(): Unit =
    pendingEdits.synchronized {
			if( pendingEditCount > 0 ) {
				pendingEdits.end()
				pendingEdits.undo()
				pendingEdits = new CompoundEdit()
				pendingEditCount = 0
			}
		}

	/** Purges the undo history and updates the undo / redo actions enabled / disabled state. */
  final def clear(): Unit = peer.discardAllEdits()

  final def add(edit: UndoableEdit): Boolean = peer.addEdit(edit)

  final def undo      (): Unit = peer.undo()
  final def redo      (): Unit = peer.redo()
  final def undoOrRedo(): Unit = peer.undoOrRedo()

  final def canUndo       : Boolean = peer.canUndo
  final def canRedo       : Boolean = peer.canRedo
  final def canUndoOrRedo : Boolean = peer.canUndoOrRedo

  final def significant   : Boolean = peer.isSignificant

	private def updateStates(): Unit = {
    val cu = peer.canUndo
    if (undoAction.enabled != cu) {
      undoAction.enabled  = cu
      dirty               = cu
    }
    val cr = peer.canRedo
    if (redoAction.enabled != cr) redoAction.enabled = cr

    val textUndo = peer.getUndoPresentationName
    if (textUndo != undoAction.title) undoAction.title = textUndo

    val textRedo = peer.getRedoPresentationName
    if (textRedo != redoAction.title) redoAction.title = textRedo
  }

  //	protected def editToBeRedone(): UndoableEdit = super.editToBeRedone()

  //	protected def editToBeUndone(): UndoableEdit = super.editToBeUndone()

  //	protected java.util.List getEdits()
  //	{
  //		return edits;
  //	}

  object undoAction extends Action("Undo") {
    accelerator = Some(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Window.menuShortcut))

    def apply(): Unit =
			try {
				undo()
			} catch{
        case e1: CannotUndoException => Console.err.println(e1.getLocalizedMessage)
      }
	}

	object redoAction extends Action("Redo") {
    val isMac = sys.props("os.name").contains("Mac OS")

    accelerator = Some(if (isMac)
      KeyStroke.getKeyStroke(KeyEvent.VK_Z, Window.menuShortcut | InputEvent.SHIFT_MASK)
    else
      KeyStroke.getKeyStroke(KeyEvent.VK_Y, Window.menuShortcut)
    )

		def apply(): Unit =
			try {
				redo()
			} catch {
        case e1: CannotRedoException => Console.err.println(e1.getLocalizedMessage)
			}
	}

	private object ActionDebugDump extends Action("Debug Undo History") {
		def apply(): Unit = {
			val num			  = manager.peer._edits.size
			val redoEdit	= manager.peer.editToBeRedone()
			val undoEdit	= manager.peer.editToBeUndone()

			Console.err.println(s"Undo buffer contains $num edits.")

      for(i <- 0 until num) {
				val edit = manager.peer._edits.get(i)
        Console.err.print(if( edit == redoEdit ) "R"
				  else if( edit == undoEdit ) "U"
          else " "
        )
				Console.err.println(s" edit #${i+1} = ${edit.getPresentationName}")
			}
		}
	}
}