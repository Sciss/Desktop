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
    ???
  }

  def removeWindow(w: Window) {
    ???
  }

  def windows: Iterator[Window] = ???

  def usesInternalFrames: Boolean = ???

  def usesScreenMenuBar: Boolean = ???

  def usesFloatingPalettes: Boolean = ???

  def setDefaultBorrower(w: Window) {
    ???
  }
}