import CustomCodeDevReleaseSteps._
import sbtrelease._
import ReleaseStateTransformations._
import ReleasePlugin._
import ReleaseKeys._
import sbt._

name := "stackmob-customcode-dev"

organization := "com.stackmob"

scalaVersion := "2.10.1"

scalacOptions := Seq("-unchecked", "-deprecation", "-feature")

libraryDependencies ++= {
    val customCodeVsn = "0.5.6"
    val jettyVsn = "7.5.4.v20111024"
    val newmanVsn = "0.14.0"
    Seq (
        "com.stackmob" % "customcode" % customCodeVsn,
        "com.twitter" %% "util-core" % "6.3.0",
        "org.scalaz" %% "scalaz-core" % "6.0.4",
        "org.specs2" %% "specs2" % "1.12.3" % "test",
        "org.scalacheck" %% "scalacheck" % "1.10.1" % "test",
        "org.eclipse.jetty" % "jetty-server" % jettyVsn,
        "net.liftweb" %% "lift-json" % "2.5-RC2",
        "com.stackmob" % "stackmob-java-client-sdk" % "1.2.0",
        "com.stackmob" %% "newman" % newmanVsn exclude("commons-codec", "commons-codec") withSources(),
        "com.stackmob" %% "newman" % newmanVsn % "test" classifier("test") withSources(),
        "com.google.guava" % "guava" % "14.0.1",
        "net.liftweb" %% "lift-json-scalaz" % "2.5-RC2",
        "org.slf4j" % "slf4j-api" % "1.7.2",
        "ch.qos.logback" % "logback-classic" % "1.0.9",
        "org.mockito" % "mockito-core" % "1.9.5" % "test",
        "org.pegdown" % "pegdown" % "1.2.1" % "test"
    )
}

logBuffered := false

org.scalastyle.sbt.ScalastylePlugin.Settings

net.virtualvoid.sbt.graph.Plugin.graphSettings

releaseSettings

releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    setReadmeReleaseVersion,
    tagRelease,
    publishArtifacts,
    setNextVersion,
    commitNextVersion,
    pushChanges
)

publishTo <<= (version) { version: String =>
    val nexus = "https://oss.sonatype.org/"
    if (version.trim.endsWith("SNAPSHOT")) {
        Some("snapshots" at nexus + "content/repositories/snapshots")
    } else {
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
    }
}

publishMavenStyle := true

publishArtifact in Test := true

testOptions in Test += Tests.Argument("html", "console")

pomIncludeRepository := { _ => false }

pomExtra := (
    <url>https://github.com/stackmob/stackmob-customcode-dev</url>
    <licenses>
        <license>
            <name>Apache 2</name>
            <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
            <distribution>repo</distribution>
        </license>
    </licenses>
    <scm>
        <url>git@github.com:stackmob/stackmob-customcode-dev.git</url>
        <connection>scm:git:git@github.com:stackmob/stackmob-customcode-dev.git</connection>
    </scm>
    <developers>
        <developer>
            <id>arschles</id>
            <name>Aaron Schlesinger</name>
            <url>http://github.com/arschles</url>
        </developer>
    </developers>
)