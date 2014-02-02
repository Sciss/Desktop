package de.sciss.desktop.mac

import com.apple.eawt.{OpenFilesHandler, QuitResponse, QuitHandler, PreferencesHandler, AboutHandler}
import javax.swing.{BoxLayout, JButton, Timer, AbstractButton, WindowConstants, JFrame}
import java.awt.{Color, LinearGradientPaint, Image, EventQueue}
import com.apple.eawt.AppEvent.{OpenFilesEvent, QuitEvent, PreferencesEvent, AboutEvent}
import java.awt.event.{ActionEvent, ActionListener}
import com.apple.eio.FileManager
import java.io.File
import java.awt.image.BufferedImage

object MacTest extends App with Runnable {
  sys.props.put("com.apple.mrj.application.apple.menu.about.name", "MacAdaptor")

  lazy val app = com.apple.eawt.Application.getApplication

  def installAbout(): Unit =
    app.setAboutHandler(new AboutHandler {
      def handleAbout(e: AboutEvent): Unit = {
        println("ABOUT")
      }
    })

  def setDockBadge(label: Option[String]): Unit =
    app.setDockIconBadge(label.orNull)

  def setDockImage(image: Image): Unit =
    app.setDockIconImage(image)

  def installPrefs(): Unit =
    app.setPreferencesHandler(new PreferencesHandler {
      def handlePreferences(e: PreferencesEvent): Unit =
        println("PREFERENCES")
    })

  def requestAttention(repeat: Boolean = false): Unit =
    app.requestUserAttention(repeat)

  def requestForeground(allWindows: Boolean = false): Unit =
    app.requestForeground(allWindows)

  def installQuit(): Unit =
    app.setQuitHandler(new QuitHandler {
      def handleQuitRequestWith(e: QuitEvent, response: QuitResponse): Unit = {
        println("QUIT")
        response.performQuit()
      }
    })

  def installOpenFiles(): Unit =
    app.setOpenFileHandler(new OpenFilesHandler {
      def openFiles(e: OpenFilesEvent): Unit = {
        println("OPEN FILES")
        println(s"search term = '${e.getSearchTerm}'")
        import scala.collection.JavaConversions._
        println(s"files = ${e.getFiles.mkString("[", ", ", "]")}")
      }
    })

  def moveToTrash(f: File): Unit =
    FileManager.moveToTrash(f)

  def revealInFinder(f: File): Unit =
    FileManager.revealInFinder(f)

  def test(): Unit = {
    // app.addAppEventListener(_ : AppEventListener)
    // app.disableSuddenTermination()
    // app.enableSuddenTermination()
    // app.getDockIconImage
    // app.getDockMenu
    // app.openHelpViewer()
    // app.removeAppEventListener(_ : AppEventListener)
    // app.requestForeground(allWindows: Boolean)
    // app.requestUserAttention(repeat: Boolean)
    // app.setDefaultMenuBar(_ : JMenuBar)
    // app.setDockIconBadge(label: String)
    // app.setDockIconImage(image: Image)
    // app.setDockMenu(m: java.awt.PopupMenu)
    // app.setOpenFileHandler(_: OpenFilesHandler)
    // app.setOpenURIHandler(_ : OpenURIHandler)
    // app.setPreferencesHandler(_ : PreferencesHandler)
    // app.setPrintFileHandler(_ : PrintFilesHandler)
    // app.setQuitHandler(_ : QuitHandler)
    // app.setQuitStrategy(_ : QuitStrategy)
    // app.requestToggleFullScreen(_ : java.awt.Window)
  }

  EventQueue.invokeLater(this)

  def count(b: AbstractButton)(action: => Unit): Unit = {
    val lbOrig = b.getText
    var i = 0
    lazy val cnt: Timer = new Timer(1000, new ActionListener {
      def actionPerformed(e: ActionEvent): Unit = {
        i += 1
        if (i == 4) {
          i = 0
          b.setText(lbOrig)
          cnt.stop()
          action
        } else {
          b.setText(i.toString)
        }
      }
    })
    cnt.setRepeats(true)
    b.addActionListener(new ActionListener {
      def actionPerformed(e: ActionEvent): Unit = cnt.restart()
    })
  }

  def run(): Unit = {
    installAbout()
    installPrefs()
    installQuit()
    installOpenFiles()

    val bImg = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB)
    val g = bImg.createGraphics()
    g.setPaint(new LinearGradientPaint(0f, 0f, 512f, 512f, Array(0f, 1f), Array(Color.red, Color.blue)))
    g.fillRect(0, 0, 512, 512)
    g.dispose()

    // setDockImage(bImg)
    // setDockImage(null)

    new JFrame("Aux") {
      setSize(400, 400)
      setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)
      setResizable(false)
      setVisible(true)
    }

    new JFrame("MacAdaptor") {
      val cp = getContentPane
      val ggAttFalse = new JButton("Attention (false)")
      count(ggAttFalse)(app.requestUserAttention(false))
      val ggAttTrue = new JButton("Attention (true)")
      count(ggAttTrue)(app.requestUserAttention(true))
      val ggFgFalse = new JButton("Foreground (false)")
      count(ggFgFalse)(app.requestForeground(false))
      val ggFgTrue = new JButton("Foreground (true)")
      count(ggFgTrue)(app.requestForeground(true))
      cp.setLayout(new BoxLayout(cp, BoxLayout.Y_AXIS))
      cp.add(ggAttFalse)
      cp.add(ggAttTrue)
      cp.add(ggFgFalse)
      cp.add(ggFgTrue)


      pack() // setSize(400, 400)
      setLocationRelativeTo(null)
      setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
      setVisible(true)
    }
  }
}
