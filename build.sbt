lazy val baseName = "Desktop"

def baseNameL = baseName.toLowerCase

lazy val projectVersion     = "0.7.3"
lazy val mimaVersion        = "0.7.0"

// ---- main dependencies ----

lazy val modelVersion       = "0.3.3"
lazy val swingPlusVersion   = "0.2.2"
lazy val fileUtilVersion    = "1.1.2"
lazy val orangeVersion      = "1.3.0"

// ---- test dependencies ----

lazy val subminVersion      = "0.2.1"

lazy val commonSettings = Seq(
  version         := projectVersion,
  organization    := "de.sciss",
  scalaVersion    := "2.11.8",
  crossScalaVersions := Seq("2.12.1", "2.11.8", "2.10.6"),
  homepage        := Some(url(s"https://github.com/Sciss/$baseName")),
  licenses        := Seq("LGPL v2.1+" -> url("http://www.gnu.org/licenses/lgpl-2.1.txt")),
  initialCommands in console := """import de.sciss.desktop._; import de.sciss.file._""",
  // retrieveManaged := true,
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
      <url>git@github.com:Sciss/{n}.git</url>
      <connection>scm:git:git@github.com:Sciss/{n}.git</connection>
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

lazy val root = Project(id = "root", base = file("."))
  .aggregate(core, linux, mac)
  .dependsOn(core, linux, mac)
  .settings(commonSettings)
  .settings(
    packagedArtifacts := Map.empty           // prevent publishing anything!
  )

lazy val core = Project(id = s"$baseNameL", base = file("core"))
  .enablePlugins(BuildInfoPlugin)
  .settings(commonSettings)
  .settings(
    name        := s"$baseName",
    description := "A library for document based desktop applications",
    libraryDependencies ++= Seq(
      "de.sciss" %% "model"     % modelVersion,
      "de.sciss" %% "swingplus" % swingPlusVersion,
      "de.sciss" %% "fileutil"  % fileUtilVersion,
      "de.sciss" %  "submin"    % subminVersion % "test"
    ),
    mimaPreviousArtifacts := Set("de.sciss" %% baseNameL % mimaVersion),
    // ---- build info ----
    buildInfoKeys := Seq(name, organization, version, scalaVersion, description,
      BuildInfoKey.map(homepage) { case (k, opt)           => k -> opt.get },
      BuildInfoKey.map(licenses) { case (_, Seq((lic, _))) => "license" -> lic }
    ),
    buildInfoPackage := "de.sciss.desktop"
  )

lazy val linux = Project(id = s"$baseNameL-linux", base = file("linux"))
  .dependsOn(core)
  .settings(commonSettings)
  .settings(
    name        := s"$baseName-linux",
    description := "Linux specific adaptors for Desktop",
    mimaPreviousArtifacts := Set("de.sciss" %% s"$baseNameL-linux" % mimaVersion)
  )

lazy val mac = Project(id = s"$baseNameL-mac", base = file("mac"))
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
      if (eawt) Nil else Seq("com.yuvimasory" % "orange-extensions" % orangeVersion % "provided")
    },
    mimaPreviousArtifacts := Set("de.sciss" %% s"$baseNameL-mac" % mimaVersion)
  )
