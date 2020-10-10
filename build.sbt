import Dependencies._

version := "0.1"
scalaVersion := "2.13.3"

herokuAppName in Compile := "seasonnow"
herokuJdkVersion in Compile := "11"
herokuProcessTypes in Compile := Map(
  "web" -> "java -jar target/universal/stage/lib/seasonnow.seasonnow-0.1.jar"
)

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "SeasonNow",
    libraryDependencies ++= akka ++ logging ++ serialization
  )