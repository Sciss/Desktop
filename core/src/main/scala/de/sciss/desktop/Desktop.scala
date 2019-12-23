/*
 *  Desktop.scala
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

package de.sciss.desktop

import java.awt
import java.net.URI

import de.sciss.file.File
import de.sciss.model.Model

import scala.collection.immutable.{IndexedSeq => Vec}
import scala.concurrent.Future
import scala.swing.Image
import scala.util.Success

object Desktop {
  private val osName: String = sys.props("os.name")

  /** `true` when running the application on a Linux system. */
  val isLinux  : Boolean = osName.contains("Linux")
  /** `true` when running the application on a Mac (OS X) system. */
  val isMac    : Boolean = osName.contains("Mac")
  /** `true` when running the application on a Windows system. */
  val isWindows: Boolean = osName.contains("Windows")

  private def getModule[A](name: String): A = {
    val clz = Class.forName(s"de.sciss.desktop.impl.$name$$")
    clz.getField("MODULE$").get(null).asInstanceOf[A]
  }

  /** The major part of the Java runtime version, such as `8` or `11`. */
  val majorJavaVersion: Int = {
    val s = sys.props("java.version")
    val m = if (s.startsWith("1.")) {
      s.substring(2, 3)
    } else {
      val i = s.indexOf(".")
      if (i > 0) s.substring(0, i) else s
    }
    try {
      m.toInt
    } catch {
      case _: NumberFormatException => 0
    }
  }

  private[desktop] lazy val platform: Platform =
    try {
      // Note: we don't have any Desktop API on Linux (OpenJDK 11) that we could use.
      // (otherwise, we could add a LinuxJava9Platform)
      if      (isLinux)                 getModule[Platform]("LinuxPlatform")
      else if (isMac && hasEAWT) {
        if      (hasClassicEAWT)        getModule[Platform]("MacPlatform")
        else if (majorJavaVersion >= 9) getModule[Platform]("MacJava9Platform")
        else                            impl.DummyPlatform
      } else if (majorJavaVersion >= 9) getModule[Platform]("Java9Platform")
      else impl.DummyPlatform

    } catch {
      case _: Throwable => impl.DummyPlatform
    }

  private[this] def hasEAWT: Boolean = try {
    Class.forName("com.apple.eawt.Application")
    true
  } catch {
    case _: ClassNotFoundException => false
  }

  private[this] def hasClassicEAWT: Boolean = try {
    Class.forName("com.apple.eawt.QuitResponse")
    true
  } catch {
    case _: ClassNotFoundException => false
  }

  // ---- platform ----

  /** Reveals a file in the platform's desktop environment. On OS X this shows the file in the Finder. */
  def revealFile     (file: File): Unit = platform revealFile      file
  /** Tries to move a file to the platform's symbolic trash can. */
  def moveFileToTrash(file: File): Unit = platform moveFileToTrash file

  /** Marks the application's icon in the desktop environment's dock with a "badge" string. For example,
    * a mail application may want to indicate the number of unread messages.
    *
    * @param  label the text label to display, or `None` to remove the current badge.
    */
  def setDockBadge(label: Option[String]): Unit = platform setDockBadge label

  /** Sets the application's appearance in the desktop environment's dock.
    * This can also be used to create non-textual badges or overlays (e.g. a progress indicator).
    *
    * @param image  the new image to use in the dock
    */
  def setDockImage(image: Image): Unit = platform setDockImage image

  /** Requests that the desktop environment signalize that the user should pay attention to the application.
    *
    * @param repeat if `true`, the signalization is continuous until the user confirms the request, if `false`
    *               the signalization is a one time action and less intrusive
    */
  def requestUserAttention(repeat: Boolean = false): Unit = platform requestUserAttention repeat

  /** Requests that the application be brought to the foreground.
    *
    * @param allWindows if `true` then all of the application's windows should be made visible and brought to the
    *                   foreground, if `false` only (the most recent?) one window is affected.
    */
  def requestForeground    (allWindows: Boolean = false): Unit = platform requestForeground    allWindows

  // ---- forwarders to awt.Desktop ----

  private def jDesktop = awt.Desktop.getDesktop

  /** Launches the associated (external) application to open the file.
    *
    * @param file   the file to be opened with the associated application
    *
    * @see [[java.awt.Desktop#open]]
    */
  def openFile(file: File): Unit = jDesktop open file

  /** Launches the associated editor application and opens a file for editing.
    *
    * @param file   the file to be opened for editing
    *
    * @see [[java.awt.Desktop#edit]]
    */
  def editFile(file: File): Unit = jDesktop edit file

  /** Prints a file with the native desktop printing facility, using the associated application's print command.
    *
    * @param file   the file to be printed
    *
    * @see [[java.awt.Desktop#print]]
    */
  def printFile(file: File): Unit = jDesktop print file

  /** Launches the default browser to display a `URI`.
    *
    * @param uri  the URI to be displayed in the user default browser
    *
    * @see [[java.awt.Desktop#browse]]
    */
  def browseURI(uri: URI): Unit = jDesktop browse uri

  /** Launches the mail composing window of the user default mail client.
    *
    * @param uri  the specified `mailto:` URI, or `None` to open the mail composing window without a specific address
    *
    * @see [[java.awt.Desktop#mail(URI)]]
    */
  def composeMail(uri: Option[URI] = None): Unit = uri.fold(jDesktop.mail())(jDesktop.mail)

  // ---- events ----

  type Listener = Model.Listener[Update]
  /** The type of updates dispatched by this desktop model to instances registered via `addListener`. */
  sealed trait Update
  /** The application was activated or brought to the front. */
  case object ApplicationActivated   extends Update
  /** The application was deactivated or moved to the background. */
  case object ApplicationDeactivated extends Update
  /** The application and its windows were made visible. */
  case object ApplicationShown       extends Update
  /** The application and its windows were hidden.
    * For example, on OS X this corresponds to the "Hide..." action in the application menu.
    */
  case object ApplicationHidden      extends Update

  /** One or several files which are associated with the application were opened from the desktop environment.
    *
    * @param search an optional search term the user entered to find the files
    * @param files  the files which should be opened by the application
    */
  case class OpenFiles(search: Option[String], files: List[File]) extends Update

  /** Registers a listener for desktop and application events. The listener receives update of type `Update`.
    *
    * @param pf   the partial reaction function
    * @return     `pf` for convenience
    */
  def addListener   (pf: Listener): pf.type = platform addListener    pf
  def removeListener(pf: Listener): Unit    = platform removeListener pf

  private val sync                                    = new AnyRef
  private var quitAcceptors: Vec[() => Future[Unit]]  = Vector.empty

  private[desktop] lazy val isQuitSupported: Boolean = initQuit()

  private def initQuit(): Boolean = platform.setQuitHandler(mayQuit())

  /** Adds a veto function invoked when calling `mayQuit`. The function should return `true` if it is ok to quit,
    * and `false` if not (for example, because a document is dirty and a confirmation dialog was cancelled).
    *
    * @param accept   the function to invoke when attempting to quit.
    * @return         the function argument for convenience
    */
  def addQuitAcceptor(accept: => Future[Unit]): () => Future[Unit] = sync.synchronized {
    isQuitSupported
    val fun = () => accept
    quitAcceptors :+= fun
    fun
  }

  def removeQuitAcceptor(accept: () => Future[Unit]): Unit = sync.synchronized {
    val idx = quitAcceptors.indexOf(accept)
    if (idx >= 0) quitAcceptors = quitAcceptors.patch(idx, Nil, 1)
  }

  /** Traverses the registered quit interceptors. If all of them accept the quit action,
    * successfully completes the future. If any
    * of them refuses the request, aborts the future with failure.
    */
  def mayQuit(): Future[Unit] = {
    val allAcc = sync.synchronized(quitAcceptors)

    def loop(in: Future[Unit], rem: List[() => Future[Unit]]): Future[Unit] = rem match {
      case Nil => in
      case head :: tail =>
        in.value match {
          case Some(Success(())) =>
            val andThen = head.apply()
            loop(andThen, tail)

          case _ =>
            import scala.concurrent.ExecutionContext.Implicits.global
            in.flatMap { _ =>
              val andThen = head.apply()
              loop(andThen, tail)
            }
        }
    }

    loop(Future.successful(()), allAcc.toList)
  }
}