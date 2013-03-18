package de.sciss.desktop
package impl

//object WindowHandlerImpl {
//  private final class DialogWindow(dialog: Dialog) extends WindowImpl {
//// 			if( modal ) fph.addModalDialog(); // this shit is necessary because java.awt.FileDialog doesn't fire windowActivated ...
//    visible = true
//// 			if( modal ) fph.removeModalDialog();
//    dispose()
//  }
//}
final class WindowHandlerImpl(val application: SwingApplication) extends WindowHandler {
  private var _windows = Vector.empty[Window]

  private val isMac = sys.props("os.name").contains("Mac OS")

  def showDialog[A](window: Window, source: DialogSource[A]): A = {
 		// temporarily disable alwaysOnTop
 		val wasOnTop = if (!usesInternalFrames && usesFloatingPalettes) windows.filter { w =>
       val res = window.alwaysOnTop
       if (res) window.alwaysOnTop = false
       res
    } .toList else Nil

 		try {
 			source.show()
 		} finally { // make sure to restore original state
       wasOnTop.foreach(_.alwaysOnTop = true)
 		}
 	}

  def addWindow(w: Window) {
    _windows :+= w
  }

  def removeWindow(w: Window) {
    val i = _windows.indexOf(w)
    if (i >= 0) _windows = _windows.patch(i, Vector.empty, 1)
  }

  def windows: Iterator[Window] = _windows.iterator

  def usesInternalFrames: Boolean = false // XXX TODO

  def usesScreenMenuBar: Boolean = isMac

  def usesFloatingPalettes: Boolean = true  // XXX TODO

  def setDefaultBorrower(w: Window) {
    // XXX TODO
  }
}