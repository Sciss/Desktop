/*
 *  MacPlatform.scala
 *  (Desktop)
 *
 *  Copyright (c) 2013-2019 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.desktop.impl

import java.io.File

import com.apple.eio.FileManager
import de.sciss.desktop.{Desktop, Platform}
import de.sciss.model.impl.ModelImpl
import com.apple.eawt
import com.apple.eawt.{AboutHandler, AppForegroundListener, AppHiddenListener, OpenFilesHandler, PreferencesHandler, QuitHandler, QuitResponse}
import com.apple.eawt.AppEvent.{AboutEvent, AppForegroundEvent, AppHiddenEvent, OpenFilesEvent, PreferencesEvent, QuitEvent}

import scala.collection.JavaConverters
import scala.concurrent.Future
import scala.swing.Image
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

/** The "classic" Mac platform with full eawt API (including Apple event types). */
object MacPlatform extends Platform with ModelImpl[Desktop.Update] {
  override def toString = "MacPlatform"

  private lazy val app = eawt.Application.getApplication

  def revealFile     (file: File): Unit = FileManager.revealInFinder(file)
  def moveFileToTrash(file: File): Unit = FileManager.moveToTrash   (file)

  def setDockBadge(label: Option[String]): Unit = app.setDockIconBadge(label.orNull)
  def setDockImage(image: Image         ): Unit = app.setDockIconImage(image)

  def requestUserAttention (repeat    : Boolean): Unit = app.requestUserAttention(repeat)
  def requestForeground    (allWindows: Boolean): Unit = app.requestForeground   (allWindows)

  private lazy val _init: Unit = init()

  private def invoke(receiver: AnyRef, method: String, arg0: AnyRef): Unit = {
    try {
      val m = receiver.getClass.getMethods.find(_.getName == method).get
      m.invoke(receiver, arg0)
    } catch {
      case NonFatal(_) => // ignore
    }
  }

  private def init(): Unit = {
    // the following events are fired on the event dispatch thread
    val afl = new AppForegroundListener {
      def appRaisedToForeground(e: AppForegroundEvent): Unit = dispatch(Desktop.ApplicationActivated  )
      def appMovedToBackground (e: AppForegroundEvent): Unit = dispatch(Desktop.ApplicationDeactivated)
    }
    invoke(app, "addAppEventListener", afl)
    val ahl = new AppHiddenListener {
      def appUnhidden          (e: AppHiddenEvent    ): Unit = dispatch(Desktop.ApplicationShown      )
      def appHidden            (e: AppHiddenEvent    ): Unit = dispatch(Desktop.ApplicationHidden     )
    }
    invoke(app, "addAppEventListener", ahl)
    val ofh = new OpenFilesHandler {
      def openFiles(e: OpenFilesEvent): Unit = {
        // println(s"openFiles. EDT? ${java.awt.EventQueue.isDispatchThread}")
        import JavaConverters._
        // the `asInstanceOf` is necessary because while the Apple library uses generic Java,
        // OrangeExtensions don't, so they return a `java.util.List<?>`
        val sq = e.getFiles.asScala.toList .asInstanceOf[List[File]]
        dispatch(Desktop.OpenFiles(Option(e.getSearchTerm), sq))
      }
    }
    invoke(app, "setOpenFileHandler", ofh)

    // sys.addShutdownHook {
    //   println("Shutdown")
    // }
  }

  def setQuitHandler(test: => Future[Unit]): Boolean = {
    val qh = new QuitHandler {
      def handleQuitRequestWith(e: QuitEvent, response: QuitResponse): Unit = {
        import scala.concurrent.ExecutionContext.Implicits.global
        test.onComplete {
          case Success(())  => response.performQuit()
          case Failure(_)   => response.cancelQuit ()
        }
      }
    }
    invoke(app, "setQuitHandler", qh)
    true
  }

  def setAboutHandler(action: => Unit): Boolean = {
    val ah = new AboutHandler {
      def handleAbout(e: AboutEvent): Unit = action
    }
    invoke(app, "setAboutHandler", ah)
    true
  }

  def setPreferencesHandler(action: => Unit): Boolean = {
    val ph = new PreferencesHandler {
      def handlePreferences(e: PreferencesEvent): Unit = action
    }
    invoke(app, "setPreferencesHandler", ph)
    true
  }

  override protected def startListening(): Unit = _init
}
