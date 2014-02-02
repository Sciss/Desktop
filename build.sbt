lazy val baseName = "Desktop"

def baseNameL = baseName.toLowerCase

lazy val commonSettings = Project.defaultSettings ++ Seq(
  version         := "0.4.0-SNAPSHOT",
  organization    := "de.sciss",
  scalaVersion    := "2.10.3",
  homepage        := Some(url("https://github.com/Sciss/" + baseName)),
  licenses        := Seq("LGPL v3+" -> url("http://www.gnu.org/licenses/lgpl-3.0.txt")),
  initialCommands in console := """import de.sciss.desktop._""",
  retrieveManaged := true,
  scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature"),
  // ---- publishing ----
  publishMavenStyle := true,
  publishTo := {
    Some(if (version.value endsWith "-SNAPSHOT")
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

lazy val core = Project(
  id        = s"$baseNameL-core",
  base      = file("core"),
  settings  = commonSettings ++ buildInfoSettings ++ Seq(
    name        := s"$baseName-core",
    description := "A library for document based desktop applications",
    libraryDependencies ++= Seq(
      "org.scala-lang"          %  "scala-swing"       % scalaVersion.value,
      "de.sciss"                %% "model"             % "0.3.+",
      "de.sciss"                %% "swingplus"         % "0.0.3+",
      "com.github.benhutchison" %  "scalaswingcontrib" % "1.5"   // using popup menu
    ),
    // ---- build info ----
    sourceGenerators in Compile <+= buildInfo,
    buildInfoKeys := Seq(name, organization, version, scalaVersion, description,
      BuildInfoKey.map(homepage) { case (k, opt)           => k -> opt.get },
      BuildInfoKey.map(licenses) { case (_, Seq((lic, _))) => "license" -> lic }
    ),
    buildInfoPackage := "de.sciss.desktop"
  )
)

// ---- ls.implicit.ly ----

seq(lsSettings :_*)

(LsKeys.tags   in LsKeys.lsync) := Seq("swing", "desktop", "application")

(LsKeys.ghUser in LsKeys.lsync) := Some("Sciss")

(LsKeys.ghRepo in LsKeys.lsync) := Some(baseName)

