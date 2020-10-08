import sbt._

object Dependencies {
  val akkaVersion = "2.6.9"

  lazy val akkaActor = "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion
  lazy val akkaStream = "com.typesafe.akka" %% "akka-stream-typed" % akkaVersion

  lazy val akka: Seq[ModuleID] = Seq(akkaActor, akkaStream)
}
