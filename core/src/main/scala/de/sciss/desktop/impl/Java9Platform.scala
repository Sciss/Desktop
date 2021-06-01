/*
 *  Java9Platform.scala
 *  (Desktop)
 *
 *  Copyright (c) 2013-2021 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.desktop.impl

import java.awt.desktop.{AboutEvent, AboutHandler, AppForegroundEvent, AppForegroundListener, AppHiddenEvent, AppHiddenListener, OpenFilesEvent, OpenFilesHandler, PreferencesEvent, PreferencesHandler, QuitEvent, QuitHandler, QuitResponse}
import java.awt.{Desktop => AWTDesktop}

import de.sciss.desktop.{Desktop, Platform}
import de.sciss.file.File
import de.sciss.model.impl.ModelImpl

import scala.collection.JavaConverters
import scala.concurrent.Future
import scala.swing.Image
import scala.util.{Failure, Success}

/** The fall back "platform" for standard Java 9 or newer API. */
object Java9Platform extends Java9Platform {
  override def toString = "Java9Platform"
}

class Java9Platform extends Platform with ModelImpl[Desktop.Update] {
  private lazy val _init: Unit = init()

  private def init(): Unit = {
    val d = AWTDesktop.getDesktop
    d.addAppEventListener(new AppForegroundListener {
      def appRaisedToForeground(e: AppForegroundEvent): Unit = dispatch(Desktop.ApplicationActivated  )
      def appMovedToBackground (e: AppForegroundEvent): Unit = dispatch(Desktop.ApplicationDeactivated)
    })
    d.addAppEventListener(new AppHiddenListener {
      def appUnhidden          (e: AppHiddenEvent    ): Unit = dispatch(Desktop.ApplicationShown      )
      def appHidden            (e: AppHiddenEvent    ): Unit = dispatch(Desktop.ApplicationHidden     )
    })
    if (d.isSupported(AWTDesktop.Action.APP_OPEN_FILE)) d.setOpenFileHandler(new OpenFilesHandler {
      def openFiles(e: OpenFilesEvent): Unit = {
        import JavaConverters._
        val sq = e.getFiles.asScala.toList
        dispatch(Desktop.OpenFiles(Option(e.getSearchTerm), sq))
      }
    })
  }

  def revealFile(file: File): Unit = {
    val d = AWTDesktop.getDesktop
    if (d.isSupported(AWTDesktop.Action.BROWSE_FILE_DIR)) {
      d.browseFileDirectory(file)
    }
  }

  def moveFileToTrash(file: File): Unit = {
    val d = AWTDesktop.getDesktop
    if (d.isSupported(AWTDesktop.Action.MOVE_TO_TRASH)) {
      d.moveToTrash(file)
    }
  }

  /** Nop */
  def setDockBadge(label: Option[String]): Unit = ()

  /** Nop */
  def setDockImage(image: Image): Unit = ()

  /** Nop */
  def requestUserAttention(repeat: Boolean): Unit = ()

  def requestForeground(allWindows: Boolean): Unit = {
    val d = AWTDesktop.getDesktop
    if (d.isSupported(AWTDesktop.Action.APP_REQUEST_FOREGROUND)) {
      d.requestForeground(allWindows)
    }
  }

  def setQuitHandler(test: => Future[Unit]): Boolean = {
    val d = AWTDesktop.getDesktop
    val ok = d.isSupported(AWTDesktop.Action.APP_QUIT_HANDLER)
    if (ok) d.setQuitHandler(new QuitHandler {
      def handleQuitRequestWith(e: QuitEvent, response: QuitResponse): Unit = {
        import scala.concurrent.ExecutionContext.Implicits.global
        test.onComplete {
          case Success(())  => response.performQuit()
          case Failure(_)   => response.cancelQuit ()
        }
      }
    })
    ok
  }

  def setAboutHandler(action: => Unit): Boolean = {
    val d = AWTDesktop.getDesktop
    val ok = d.isSupported(AWTDesktop.Action.APP_ABOUT)
    if (ok) d.setAboutHandler(new AboutHandler {
      def handleAbout(e: AboutEvent): Unit = action
    })
    ok
  }

  def setPreferencesHandler(action: => Unit): Boolean = {
    val d = AWTDesktop.getDesktop
    val ok = d.isSupported(AWTDesktop.Action.APP_PREFERENCES)
    if (ok) d.setPreferencesHandler(new PreferencesHandler {
      def handlePreferences(e: PreferencesEvent): Unit = action
    })
    ok
  }

  override protected def startListening(): Unit = _init
}
