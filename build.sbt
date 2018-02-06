ThisBuild / organization := "io.github.nafg.simpleivr"

lazy val core = project
  .settings(
    name := "simpleivr-core",
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "sourcecode" % "0.1.4",
      "org.typelevel" %% "cats-free" % "1.0.1",
      "org.typelevel" %% "cats-effect" % "0.8"
    )
  )

lazy val testing = project
  .dependsOn(core)
  .settings(
    name := "simpleivr-testing",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5"
  )

lazy val asterisk = project
  .dependsOn(core)
  .settings(
    name := "simpleivr-asterisk",
    libraryDependencies += "org.asteriskjava" % "asterisk-java" % "2.0.2"
  )

skip in publish := true
