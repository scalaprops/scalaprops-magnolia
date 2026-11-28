addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.6.2")
addSbtPlugin("com.github.scalaprops" % "sbt-scalaprops" % "0.7.0")
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.6.0")
addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.4.0")
addSbtPlugin("com.eed3si9n" % "sbt-nocomma" % "0.2.0")

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-language:existentials",
  "-language:implicitConversions",
)
