package de.sciss.desktop

object FocusType {
  import javax.swing.JComponent._
  case object Default   extends FocusType { def id = WHEN_FOCUSED }
  case object Window    extends FocusType { def id = WHEN_IN_FOCUSED_WINDOW }
  case object Ancestor  extends FocusType { def id = WHEN_ANCESTOR_OF_FOCUSED_COMPONENT }
}
sealed trait FocusType { def id: Int }