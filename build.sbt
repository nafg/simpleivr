ThisBuild / scalaVersion := "2.12.8"
ThisBuild / organization := "io.github.nafg.simpleivr"

def ScalaTest = "org.scalatest" %% "scalatest" % "3.0.8"

ThisBuild / scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-explaintypes",
  "-Xfuture",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ypartial-unification",
  "-Ywarn-dead-code",
  "-Ywarn-extra-implicit",
  "-Ywarn-inaccessible",
  "-Ywarn-infer-any",
  "-Ywarn-nullary-override",
  "-Ywarn-nullary-unit",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused-import",
  "-Ywarn-unused",
  "-Ywarn-value-discard"
)

lazy val core = project
  .settings(
    name := "simpleivr-core",
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "sourcecode" % "0.1.7",
      "org.typelevel" %% "cats-free" % "2.0.0",
      "org.typelevel" %% "cats-effect" % "2.0.0",
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
    libraryDependencies += "org.asteriskjava" % "asterisk-java" % "2.0.2"
  )

skip in publish := true
