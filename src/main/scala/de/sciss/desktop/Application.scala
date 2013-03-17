package de.sciss.desktop

trait Application {
  app =>
  type Document
  def quit(): Unit
  def name: String

  def addComponent(key: String, component: Any): Unit
  def removeComponent(key: String): Unit
  def getComponent[A](key: String): Option[A]
  def documentHandler: DocumentHandler {
    type Document = app.Document
  }

  def userPrefs: Preferences
  def systemPrefs: Preferences
}

trait SwingApplication extends Application {
  def windowHandler: WindowHandler
}