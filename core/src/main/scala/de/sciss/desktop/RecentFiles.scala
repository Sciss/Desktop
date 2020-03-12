/*
 *  RecentFiles.scala
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

import java.awt.event.{InputEvent, KeyEvent}
import javax.swing.KeyStroke

import de.sciss.file.File

object RecentFiles {
  private lazy val _defaultKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_O, Window.menuShortcut + InputEvent.ALT_MASK)

  def defaultKeyStroke: KeyStroke = _defaultKeyStroke

  def apply(entry: Preferences.Entry[List[File]], maxItems: Int = 10,
            keyStroke: Option[KeyStroke] = Some(defaultKeyStroke))
           (action: File => Unit): RecentFiles =
    new impl.RecentFilesImpl(entry, maxItems, action, keyStroke)
}
trait RecentFiles {
  def menu: Menu.Group
  def add   (file: File): Unit
  def remove(file: File): Unit
  def files: List[File]

  def dispose(): Unit
}