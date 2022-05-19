ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.1.2"

lazy val root = (project in file("."))
  .settings(
    name := "luxoft-exercise",
    organization := "ch.hibernator.luxoft",
    libraryDependencies ++= Seq(
      ("com.lightbend.akka" %% "akka-stream-alpakka-csv" % "3.0.4").cross(CrossVersion.for3Use2_13),
      ("com.typesafe.akka" %% "akka-stream" % "2.6.19").cross(CrossVersion.for3Use2_13),
      "org.scalactic" %% "scalactic" % "3.2.12",
      "org.scalatest" %% "scalatest" % "3.2.12" % "test"
    )
  )
