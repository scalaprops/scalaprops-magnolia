import sbtrelease._
import ReleaseStateTransformations._
import com.typesafe.sbt.pgp.PgpKeys

name := UpdateReadme.scalapropsMagnoliaName

description := "Generation of arbitrary case classes / ADTs instances with Scalaprops and Magnolia"

version := "0.1.0"

val Scala212 = "2.12.4"

scalaVersion := Scala212

crossScalaVersions := Seq(Scala212)

organization := "com.github.scalaprops"

homepage := Some(url("https://github.com/scalaprops/scalaprops-magnolia"))

licenses := Seq("MIT License" -> url("https://opensource.org/licenses/mit-license"))

def gitHash(): String = sys.process.Process("git rev-parse HEAD").lineStream_!.head

val tagName = Def.setting {
  s"v${if (releaseUseGlobalVersion.value) (version in ThisBuild).value
  else version.value}"
}
val tagOrHash = Def.setting {
  if (isSnapshot.value) gitHash() else tagName.value
}

pomExtra :=
  <developers>
    <developer>
      <id>xuwei-k</id>
      <name>Kenji Yoshida</name>
      <url>https://github.com/xuwei-k</url>
    </developer>
  </developers>
  <scm>
    <url>git@github.com:scalaprops/scalaprops-magnolia.git</url>
    <connection>scm:git:git@github.com:scalaprops/scalaprops-magnolia.git</connection>
    <tag>{tagOrHash.value}</tag>
  </scm>

val unusedWarnings = Seq("-Ywarn-unused", "-Ywarn-unused-import")

scalacOptions ++= unusedWarnings

Seq(Compile, Test).flatMap(c => scalacOptions in (c, console) --= unusedWarnings)

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:experimental.macros",
  "-unchecked",
  "-Xlint:infer-any",
  "-Xlint:missing-interpolator",
  "-Xlint:nullary-override",
  "-Xlint:nullary-unit",
  "-Xlint:private-shadow",
  "-Xlint:stars-align",
  "-Xlint:type-parameter-shadow",
  "-Xlint:unsound-match",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture"
)

scalapropsVersion := "0.5.4"

libraryDependencies ++= Seq(
  "com.propensive" %% "magnolia" % "0.7.1",
  "com.github.scalaprops" %% "scalaprops-gen" % scalapropsVersion.value,
  "com.github.scalaprops" %% "scalaprops" % scalapropsVersion.value % "test"
)

scalapropsSettings

publishTo := sonatypePublishTo.value

releaseTagName := tagName.value

releaseCrossBuild := true

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  UpdateReadme.updateReadmeProcess,
  tagRelease,
  ReleaseStep(
    action = { state =>
      val extracted = Project extract state
      extracted.runAggregated(PgpKeys.publishSigned in Global in extracted.get(thisProjectRef), state)
    },
    enableCrossBuild = true
  ),
  setNextVersion,
  commitNextVersion,
  releaseStepCommand("sonatypeReleaseAll"),
  UpdateReadme.updateReadmeProcess,
  pushChanges
)

scalacOptions in (Compile, doc) ++= {
  val tag = tagOrHash.value
  Seq(
    "-sourcepath",
    (baseDirectory in LocalRootProject).value.getAbsolutePath,
    "-doc-source-url",
    s"https://github.com/scalaprops/scalaprops-magnolia/tree/${tag}€{FILE_PATH}.scala"
  )
}
