import sbtrelease._
import ReleaseStateTransformations._
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

ThisBuild / onChangedBuildSource := ReloadOnSourceChanges

def gitHash(): String = sys.process.Process("git rev-parse HEAD").lineStream_!.head

val tagName = Def.setting {
  s"v${if (releaseUseGlobalVersion.value) (version in ThisBuild).value
  else version.value}"
}
val tagOrHash = Def.setting {
  if (isSnapshot.value) gitHash() else tagName.value
}
val Scala212 = "2.12.11"
val Scala213 = "2.13.1"
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
        extracted.runAggregated(PgpKeys.publishSigned in Global in extracted.get(thisProjectRef), state)
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

lazy val scalapropsMagnolia = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("."))
  .settings(
    scalapropsCoreSettings,
    commonSettings,
    Seq(Compile, Test).flatMap(c => scalacOptions in (c, console) --= unusedWarnings)
  )
  .settings(nocomma {
    name := UpdateReadme.scalapropsMagnoliaName
    description := "Generation of arbitrary case classes / ADTs instances with Scalaprops and Magnolia"
    scalacOptions ++= unusedWarnings
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
    scalapropsVersion := "0.6.3"
    libraryDependencies ++= Seq(
      "com.propensive" %%% "magnolia" % "0.14.5",
      "com.github.scalaprops" %%% "scalaprops-gen" % scalapropsVersion.value,
      "com.github.scalaprops" %%% "scalaprops" % scalapropsVersion.value % "test"
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
  })
  .jsSettings(
    scalacOptions += {
      val a = (baseDirectory in LocalRootProject).value.toURI.toString
      val g = "https://raw.githubusercontent.com/scalaprops/scalaprops-magnolia/" + tagOrHash.value
      s"-P:scalajs:mapSourceURI:$a->$g/"
    }
  )

lazy val notPublish = nocomma {
  publishArtifact := false
  publish := {}
  publishLocal := {}
  PgpKeys.publishSigned := {}
  PgpKeys.publishLocalSigned := {}
}

commonSettings
notPublish
name := "root"
sources in Compile := Nil
sources in Test := Nil
