package de.sciss.desktop

import java.io.File

object PrefsTest {
  def test(p: Preferences): Unit = {
    p.put("string", "value")(Preferences.Type.string)
    p.put("int", 123)
    p.put("boolean", true)
    p.put("file", new File("file"))
    p.getOrElse("string", "default")
    p.getOrElse("int", 345)
    p.getOrElse("boolean", false)
    p.getOrElse("file", new File("dir"))
  }
}