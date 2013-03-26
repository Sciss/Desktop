name := "Desktop"

version := "0.1.0"

organization := "de.sciss"

scalaVersion := "2.10.1"

description := "A library for document based desktop applications"

homepage <<= name { n => Some(url("https://github.com/Sciss/" + n)) }

licenses := Seq("GPL v2+" -> url("http://www.gnu.org/licenses/gpl-2.0.txt"))

initialCommands in console := """import de.sciss.desktop._"""

libraryDependencies in ThisBuild ++= Seq(
  "com.github.benhutchison" % "scalaswingcontrib" % "1.4"   // using popup menu
//  "org.scalatest" %% "scalatest" % "1.9.1" % "test"
)

libraryDependencies in ThisBuild <+= scalaVersion { sv =>
  "org.scala-lang" % "scala-swing" % sv
}

// ---- publishing ----

publishMavenStyle := true

publishTo <<= version { (v: String) =>
  Some(if (v endsWith "-SNAPSHOT")
    "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  else
    "Sonatype Releases"  at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
  )
}

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra <<= name { n =>
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
