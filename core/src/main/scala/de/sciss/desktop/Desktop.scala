/*
 *  Desktop.scala
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

import java.io.File
import scala.util.Try
import de.sciss.model.Model
import scala.swing.Image
import java.awt
import java.net.URI
import scala.collection.immutable.{IndexedSeq => Vec}

object Desktop {
  private val osName: String = sys.props("os.name")

  /** `true` when running the application on a Linux system. */
  val isLinux  : Boolean = osName.contains("Linux")
  /** `true` when running the application on a Mac (OS X) system. */
  val isMac    : Boolean = osName.contains("Mac")
  /** `true` when running the application on a Windows system. */
  val isWindows: Boolean = osName.contains("Windows")

  private def getModule[A](name: String): A = Class.forName(name + "$").getField("MODULE$").get(null).asInstanceOf[A]

  private[desktop] lazy val platform: Platform = {
    val res = Try {
      if (isMac) getModule[Platform]("de.sciss.desktop.impl.MacPlatform")
      // else if (isLinux) ...
      // else if (isWindows) ...
      else       impl.DummyPlatform
    } .getOrElse(impl.DummyPlatform)

    // println(s"Installed platform: '$res'")
    res
  }

  // ---- platform ----

  /** Reveals a file in the platform's desktop environment. On OS X this shows the file in the Finder. */
  def revealFile     (file: File): Unit = platform revealFile      file
  /** Tries to move a file to the plaform's symbolic trash can. */
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
  def setDockImage(image: Image         ): Unit = platform setDockImage image

  /** Requests that the desktop environment signalize that the user should pay attention to the application.
    *
    * @param repeat if `true`, the signalization is continuous until the user confirms the request, if `false`
    *               the signalization is a one time action and less intrusive
    */
  def requestUserAttention (repeat    : Boolean = false): Unit = platform requestUserAttention repeat

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

  private val sync = new AnyRef
  private var quitAcceptors = Vec.empty[() => Boolean]

  private[desktop] lazy val isQuitSupported: Boolean = initQuit()

  private def initQuit(): Boolean = platform.setQuitHandler(mayQuit())

  /** Adds a veto function invoked when calling `mayQuit`. The function should return `true` if it is ok to quit,
    * and `false` if not (for example, because a document is dirty and a confirmation dialog was cancelled).
    *
    * @param accept   the function to invoke when attempting to quit.
    * @return         the function argument for convenience
    */
  def addQuitAcceptor(accept: => Boolean): () => Boolean = sync.synchronized {
    isQuitSupported
    val fun = () => accept
    quitAcceptors :+= fun
    fun
  }

  def removeQuitAcceptor(accept: () => Boolean): Unit = sync.synchronized {
    val idx = quitAcceptors.indexOf(accept)
    if (idx >= 0) quitAcceptors = quitAcceptors.patch(idx, Nil, 1)
  }

  /** Traverses the registered quit interceptors. If all of them accept the quit action, returns `true`. If any
    * of them refuses the request, returns `false`.
    */
  def mayQuit(): Boolean = sync.synchronized(quitAcceptors.forall(_.apply()))
}