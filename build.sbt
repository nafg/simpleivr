import _root_.io.github.nafg.scalacoptions.{ScalacOptions, options}

ThisBuild / crossScalaVersions := Seq("2.12.17", "2.13.10", "3.2.1")
ThisBuild / scalaVersion := {
  val versions = (ThisBuild / crossScalaVersions).value
  if (sys.props.contains("idea.managed"))
    versions.filter(_.startsWith("2.")).last
  else
    versions.last
}
ThisBuild / organization := "io.github.nafg.simpleivr"

def ScalaTest = "org.scalatest" %% "scalatest" % "3.2.14"

ThisBuild / scalacOptions ++=
  ScalacOptions.all(scalaVersion.value)(
    (o: options.Common) =>
      o.deprecation ++
        o.feature ++
        o.unchecked,
    (o: options.V2) =>
      o.explaintypes ++ Seq(
        "-Xlint:_",
        "-Ywarn-dead-code",
        "-Ywarn-extra-implicit",
        "-Ywarn-numeric-widen",
        "-Ywarn-unused:_",
        "-Ywarn-value-discard"
      ),
    (o: options.V2_12) =>
      o.language("higherKinds") ++
        o.Xfuture ++
        o.YpartialUnification,
    (o: options.V3) =>
      o.explainTypes
  )

lazy val core = project
  .settings(
    name := "simpleivr-core",
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "sourcecode" % "0.3.0",
      "org.typelevel" %% "cats-free" % "2.9.0",
      "org.typelevel" %% "cats-effect" % "3.4.1",
      ScalaTest % Test
    )
  )

lazy val testing = project
  .dependsOn(core % "compile->compile;test->test")
  .settings(
    name := "simpleivr-testing",
    libraryDependencies += ScalaTest
  )

lazy val asterisk = project
  .dependsOn(core)
  .settings(
    name := "simpleivr-asterisk",
    libraryDependencies += "org.asteriskjava" % "asterisk-java" % "3.36.0",
    libraryDependencies += "org.scala-lang.modules" %% "scala-collection-compat" % "2.8.1"
  )

publish / skip := true
