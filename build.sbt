lazy val baseName        = "Desktop"
lazy val baseNameL       = baseName.toLowerCase
lazy val baseDescription = "A library for document based desktop applications"

lazy val projectVersion     = "0.11.3"
lazy val mimaVersion        = "0.11.0"

// ---- dependencies ----

lazy val deps = new {
  val main = new {
    val model       = "0.3.5"
    val swingPlus   = "0.5.0"
    val fileUtil    = "1.1.5"
    val orange      = "1.3.0"
  }
  val test = new {
    val submin      = "0.3.4"
  }
}

// sonatype plugin requires that these are in global
ThisBuild / version      := projectVersion
ThisBuild / organization := "de.sciss"

lazy val commonSettings = Seq(
//  version            := projectVersion,
//  organization       := "de.sciss",
  scalaVersion       := "2.13.4",
  crossScalaVersions := Seq("3.0.0-RC1", "2.13.4", "2.12.13"),
  homepage           := Some(url(s"https://git.iem.at/sciss/$baseName")),
  licenses           := Seq("LGPL v2.1+" -> url("http://www.gnu.org/licenses/lgpl-2.1.txt")),
  initialCommands in console := """import de.sciss.desktop._; import de.sciss.file._""",
  scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-encoding", "utf8", "-Xsource:2.13"),
  sources in (Compile, doc) := {
    if (isDotty.value) Nil else (sources in (Compile, doc)).value // dottydoc is currently broken
  },
) ++ publishSettings

lazy val root = project.withId(baseNameL).in(file("."))
  .aggregate(core, linux, mac)
  .dependsOn(core, linux, mac)
  .settings(commonSettings)
  .settings(
    description := baseDescription,
    publishArtifact in(Compile, packageBin) := false, // there are no binaries
    publishArtifact in(Compile, packageDoc) := false, // there are no javadocs
    publishArtifact in(Compile, packageSrc) := false, // there are no sources
    // packagedArtifacts := Map.empty
    autoScalaLibrary := false
  )

lazy val core = project.withId(s"$baseNameL-core").in(file("core"))
  .enablePlugins(BuildInfoPlugin)
  .settings(commonSettings)
  .settings(
    name        := s"$baseName-core",
    description := baseDescription,
    libraryDependencies ++= Seq(
      "de.sciss" %% "model"     % deps.main.model,
      "de.sciss" %% "swingplus" % deps.main.swingPlus,
      "de.sciss" %% "fileutil"  % deps.main.fileUtil,
      "de.sciss" %  "submin"    % deps.test.submin % Test
    ),
    mimaPreviousArtifacts := Set("de.sciss" %% s"$baseNameL-core" % mimaVersion),
    // ---- build info ----
    buildInfoKeys := Seq(name, organization, version, scalaVersion, description,
      BuildInfoKey.map(homepage) { case (k, opt)           => k -> opt.get },
      BuildInfoKey.map(licenses) { case (_, Seq((lic, _))) => "license" -> lic }
    ),
    buildInfoPackage := "de.sciss.desktop"
  )

lazy val linux = project.withId(s"$baseNameL-linux").in(file("linux"))
  .dependsOn(core)
  .settings(commonSettings)
  .settings(
    name        := s"$baseName-linux",
    description := "Linux specific adaptors for Desktop",
    mimaPreviousArtifacts := Set("de.sciss" %% s"$baseNameL-linux" % mimaVersion)
  )

lazy val mac = project.withId(s"$baseNameL-mac").in(file("mac"))
  .dependsOn(core /* platform */)
  .settings(commonSettings)
  .settings(
    name        := s"$baseName-mac",
    description := "Macintosh specific adaptors for Desktop",
    libraryDependencies ++= {
      val eawt = try {
        // Note: newer macOS still has `Application` but
        // no longer the pre-Java 9 Apple event types (which orange-extensions do have)
//          Class.forName("com.apple.eawt.Application")
          Class.forName("com.apple.eawt.QuitResponse")
          true
        } catch {
          case _: ClassNotFoundException => false
        }
      if (eawt) Nil else Seq("com.yuvimasory" % "orange-extensions" % deps.main.orange % Provided)
    },
    mimaPreviousArtifacts := Set("de.sciss" %% s"$baseNameL-mac" % mimaVersion)
  )

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  developers := List(
    Developer(
      id    = "sciss",
      name  = "Hanns Holger Rutz",
      email = "contact@sciss.de",
      url   = url("https://www.sciss.de")
    )
  ),
  scmInfo := {
    val h = "git.iem.at"
    val a = s"sciss/$baseName"
    Some(ScmInfo(url(s"https://$h/$a"), s"scm:git@$h:$a.git"))
  },
)

