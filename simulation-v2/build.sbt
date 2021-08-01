ThisBuild / scalaVersion := "2.13.6"
ThisBuild / version := "0.1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .settings(
    name := "simulation-v2",
    resolvers += ("jitpack" at "https://jitpack.io"),
    libraryDependencies ++= Seq(
      "com.github.fdietze.probability-monad" %% "probability-monad" % "837a419257883",
      "org.scalatest" %% "scalatest" % "3.2.2" % Test
    )
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
