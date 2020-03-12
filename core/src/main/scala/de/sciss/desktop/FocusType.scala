/*
 *  FocusType.scala
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

object FocusType {
  import javax.swing.JComponent._
  case object Default   extends FocusType { def id: Int = WHEN_FOCUSED }
  case object Window    extends FocusType { def id: Int = WHEN_IN_FOCUSED_WINDOW }
  case object Ancestor  extends FocusType { def id: Int = WHEN_ANCESTOR_OF_FOCUSED_COMPONENT }
}
sealed trait FocusType { def id: Int }