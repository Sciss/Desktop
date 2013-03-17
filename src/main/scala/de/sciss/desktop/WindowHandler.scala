package de.sciss.desktop

object WindowHandler {
  object Preferences {
    /**
     * Value: Boolean stating whether internal frames within one
     * big app frame are used. Has default value: no!<br>
     * Node: root
     */
    final val keyInternalFrames = "internalframes"

    /**
     * Value: Boolean stating whether palette windows should
     * be floating on top and have palette decoration. Has default value: no!<br>
     * Node: root
     */
    final val keyFloatingPalettes = "floatingpalettes"

    /**
     * Value: Boolean stating whether to use the look-and-feel (true)
     * or native (false) decoration for frame borders. Has default value: no!<br>
     * Node: root
     */
    final val keyLookAndFeelDecoration = "lafdecoration"

    final val keyIntrudingGrowBox = "intrudinggrowbox"
  }

    //  final val OPTION_EXCLUDE_FONT: AnyRef = "excludefont"
//  final val OPTION_GLOBAL_MENUBAR: AnyRef = "globalmenu"

//  def findWindow(component: Component): Option[Window] = Impl.findWindow(component)
//
//  def showDialog(dialog: Dialog)                                           { Impl.showDialog(dialog) }
//  def showDialog(parent: Component, dialog: Dialog)                        { Impl.showDialog(parent, dialog) }
//  def showDialog(parent: Component, pane: JOptionPane, title: String): Any = Impl.showDialog(parent, pane, title)
//  def showDialog(pane: JOptionPane, title: String): Any =                    Impl.showDialog(pane, title)
//  def showErrorDialog(exception: Exception, title: String)                 { Impl.showErrorDialog(exception, title) }
//
//  def showAction(w: Window): Action = Impl.showAction(w)
}
trait WindowHandler {
  def application: SwingApplication

  def addWindow   (w: Window): Unit
  def removeWindow(w: Window): Unit

  def windows: Iterator[Window]

//  def createWindow(flags: Int): Window

  def usesInternalFrames: Boolean
  def usesScreenMenuBar: Boolean
  def usesFloatingPalettes: Boolean

  def setDefaultBorrower(w: Window): Unit

  def showDialog[A](window: Window, source: DialogSource[A]): A
}