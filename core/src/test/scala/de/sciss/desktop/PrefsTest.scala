package de.sciss.desktop

import java.io.File

import de.sciss.submin.Submin
import de.sciss.swingplus.GroupPanel

import scala.swing.{Frame, MainFrame, SimpleSwingApplication}

object PrefsTest extends SimpleSwingApplication {
  override def startup(args: Array[String]): Unit = {
    val isDark = args.contains("--dark")
    Submin.install(isDark)
    super.startup(args)
  }

  lazy val top: Frame = new MainFrame {
    import PrefsGUI._
    import de.sciss.file._

    private val base    = Preferences.user(getClass)
    private val ggCheck = checkBox  (base[Boolean ]("boolean"), default = false)
    private val ggCombo = combo     (base[String  ]("string"), default = "foo", values = Seq("foo", "bar", "baz"))
    private val ggInt   = intField  (base[Int     ]("integer"), 1000)
    private val ggPath  = pathField1(base[File    ]("file"), default = userHome, title = "Select File")

    private val labels  = Seq("Boolean", "String", "Integer", "File").map(label)
    private val values  = Seq(ggCheck  , ggCombo , ggInt    , ggPath)

    private val p = new GroupPanel {
      import GroupPanel.Element
      horizontal = Seq(
        Par(labels.map(Element(_)): _*),
        Par(values.map(Element(_)): _*)
      )

      vertical = Seq(
        (labels zip values).map { case (lb, vl) =>
          Par(Center)(lb, vl)
        }: _*
      )
    }

    title = "Preferences Test"
    contents = p
  }

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