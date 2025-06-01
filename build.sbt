import sbtrelease._
import ReleaseStateTransformations._

ThisBuild / onChangedBuildSource := ReloadOnSourceChanges

def gitHash(): String = sys.process.Process("git rev-parse HEAD").lineStream_!.head

val tagName = Def.setting {
  s"v${if (releaseUseGlobalVersion.value) (ThisBuild / version).value
    else version.value}"
}
val tagOrHash = Def.setting {
  if (isSnapshot.value) gitHash() else tagName.value
}
val Scala212 = "2.12.20"
val Scala213 = "2.13.16"
val unusedWarnings = Seq("-Ywarn-unused")

lazy val commonSettings = nocomma {
  scalaVersion := Scala212
  crossScalaVersions := Seq(Scala212, Scala213)
  organization := "com.github.scalaprops"
  homepage := Some(url("https://github.com/scalaprops/scalaprops-magnolia"))
  licenses := Seq("MIT License" -> url("https://opensource.org/licenses/mit-license"))
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
  publishTo := sonatypePublishToBundle.value
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
        extracted.runAggregated(extracted.get(thisProjectRef) / (Global / PgpKeys.publishSigned), state)
      },
      enableCrossBuild = true
    ),
    releaseStepCommand("sonatypeBundleRelease"),
    setNextVersion,
    commitNextVersion,
    UpdateReadme.updateReadmeProcess,
    pushChanges
  )
}

lazy val scalapropsMagnolia = project
  .in(file("."))
  .settings(
    scalapropsCoreSettings,
    commonSettings,
    Seq(Compile, Test).flatMap(c => c / console / scalacOptions --= unusedWarnings)
  )
  .settings(nocomma {
    name := UpdateReadme.scalapropsMagnoliaName
    description := "Generation of arbitrary case classes / ADTs instances with Scalaprops and Magnolia"
    scalacOptions ++= unusedWarnings
    scalacOptions += {
      scalaBinaryVersion.value match {
        case "2.13" =>
          "-Xsource:3-cross"
        case "2.12" =>
          "-Xsource:3"
      }
    }
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
      "-Xlint:nullary-unit",
      "-Xlint:private-shadow",
      "-Xlint:stars-align",
      "-Xlint:type-parameter-shadow",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen",
      "-Ywarn-value-discard",
    )
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, v)) if v <= 12 =>
          Seq(
            "-Yno-adapted-args",
            "-Xlint:unsound-match",
            "-Xfuture",
          )
        case _ =>
          Nil
      }
    }
    scalapropsVersion := "0.10.0"
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided,
      "com.softwaremill.magnolia1_2" %% "magnolia" % "1.1.10",
      "com.github.scalaprops" %% "scalaprops-gen" % scalapropsVersion.value,
      "com.github.scalaprops" %% "scalaprops" % scalapropsVersion.value % "test"
    )
    (Compile / doc / scalacOptions) ++= {
      val tag = tagOrHash.value
      Seq(
        "-sourcepath",
        (LocalRootProject / baseDirectory).value.getAbsolutePath,
        "-doc-source-url",
        s"https://github.com/scalaprops/scalaprops-magnolia/tree/${tag}€{FILE_PATH}.scala"
      )
    }
  })
