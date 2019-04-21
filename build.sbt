lazy val baseName        = "Desktop"
lazy val baseNameL       = baseName.toLowerCase
lazy val baseDescription = "A library for document based desktop applications"

lazy val projectVersion     = "0.10.3"
lazy val mimaVersion        = "0.10.0"

// ---- dependencies ----

lazy val deps = new {
  val main = new {
    val model       = "0.3.4"
    val swingPlus   = "0.4.2"
    val fileUtil    = "1.1.3"
    val orange      = "1.3.0"
  }
  val test = new {
    val submin      = "0.2.5"
  }
}

lazy val commonSettings = Seq(
  version            := projectVersion,
  organization       := "de.sciss",
  scalaVersion       := "2.12.8",
  crossScalaVersions := Seq("2.12.8", "2.11.12", "2.13.0-RC1"),
  homepage           := Some(url(s"https://git.iem.at/sciss/$baseName")),
  licenses           := Seq("LGPL v2.1+" -> url("http://www.gnu.org/licenses/lgpl-2.1.txt")),
  initialCommands in console := """import de.sciss.desktop._; import de.sciss.file._""",
  scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-encoding", "utf8", "-Xfuture"),
  // ---- publishing ----
  publishMavenStyle := true,
  publishTo := {
    Some(if (isSnapshot.value)
      "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
    else
      "Sonatype Releases"  at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
    )
  },
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  pomExtra := { val n = baseName
    <scm>
      <url>git@git.iem.at:sciss/{n}.git</url>
      <connection>scm:git:git@git.iem.at.com:sciss/{n}.git</connection>
    </scm>
    <developers>
      <developer>
        <id>sciss</id>
        <name>Hanns Holger Rutz</name>
        <url>http://www.sciss.de</url>
      </developer>
    </developers>
  }
)

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
          Class.forName("com.apple.eawt.Application")
          true
        } catch {
          case _: ClassNotFoundException => false
        }
      if (eawt) Nil else Seq("com.yuvimasory" % "orange-extensions" % deps.main.orange % Provided)
    },
    mimaPreviousArtifacts := Set("de.sciss" %% s"$baseNameL-mac" % mimaVersion)
  )
