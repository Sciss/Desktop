lazy val baseName = "Desktop"

def baseNameL = baseName.toLowerCase

lazy val projectVersion      = "0.4.1"

lazy val modelVersion        = "0.3.1+"

lazy val swingPlusVersion    = "0.1.+"

lazy val swingContribVersion = "1.5"

lazy val commonSettings = Project.defaultSettings ++ Seq(
  version         := projectVersion,
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

lazy val root = Project(
  id        = "root",
  base      = file("."),
  aggregate = Seq(core, mac),
  settings  = Project.defaultSettings ++ Seq(
    packagedArtifacts := Map.empty           // prevent publishing anything!
  )
)

lazy val core = Project(
  id            = s"$baseNameL",
  base          = file("core"),
  // dependencies  = Seq(platform),
  settings      = commonSettings ++ buildInfoSettings ++ Seq(
    name        := s"$baseName",
    description := "A library for document based desktop applications",
    libraryDependencies ++= Seq(
      "org.scala-lang"          %  "scala-swing"       % scalaVersion.value,
      "de.sciss"                %% "model"             % modelVersion,
      "de.sciss"                %% "swingplus"         % swingPlusVersion,
      "com.github.benhutchison" %  "scalaswingcontrib" % swingContribVersion   // using popup menu
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

lazy val mac = Project(
  id            = s"$baseNameL-mac",
  base          = file("mac"),
  dependencies  = Seq(core /* platform */),
  settings      = commonSettings ++ Seq(
    name        := s"$baseName-mac",
    description := "Macintosh specific adaptors or Desktop"
  )
)

// ---- ls.implicit.ly ----

seq(lsSettings :_*)

(LsKeys.tags   in LsKeys.lsync) := Seq("swing", "desktop", "application")

(LsKeys.ghUser in LsKeys.lsync) := Some("Sciss")

(LsKeys.ghRepo in LsKeys.lsync) := Some(baseName)

