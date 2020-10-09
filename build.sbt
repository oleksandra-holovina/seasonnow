import Dependencies._

ThisBuild / version := "0.1"
ThisBuild / scalaVersion := "2.13.3"

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "SeasonNow",
    libraryDependencies ++= akka ++ logging ++ serialization
  )