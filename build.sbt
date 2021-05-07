ThisBuild / crossScalaVersions := Seq("2.12.13", "2.13.5")
ThisBuild / scalaVersion := (ThisBuild / crossScalaVersions).value.last
ThisBuild / organization := "io.github.nafg.simpleivr"

def ScalaTest = "org.scalatest" %% "scalatest" % "3.2.8"

ThisBuild / scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-explaintypes",
  "-Xlint:_",
  "-Ywarn-dead-code",
  "-Ywarn-extra-implicit",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused:_",
  "-Ywarn-value-discard"
)

ThisBuild / scalacOptions ++=
  (if (scalaVersion.value.startsWith("2.12."))
    List("-language:higherKinds", "-Xfuture", "-Ypartial-unification")
  else
    Nil)

lazy val core = project
  .settings(
    name := "simpleivr-core",
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "sourcecode" % "0.2.6",
      "org.typelevel" %% "cats-free" % "2.6.0",
      "org.typelevel" %% "cats-effect" % "3.1.0",
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
    libraryDependencies += "org.asteriskjava" % "asterisk-java" % "3.12.0",
    libraryDependencies += "org.scala-lang.modules" %% "scala-collection-compat" % "2.4.3"
  )

publish / skip := true
