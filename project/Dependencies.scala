import sbt._

object Dependencies {
  val akkaVersion = "2.6.9"
  val akkaHttpVersion = "10.2.1"

  lazy val akkaActor = "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion
  lazy val akkaStream = "com.typesafe.akka" %% "akka-stream-typed" % akkaVersion
  lazy val akkaPersistence = "com.typesafe.akka" %% "akka-persistence-typed" % akkaVersion
  lazy val akkaHttp = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion

  lazy val sprayJson = "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion

  lazy val leveldb = "org.iq80.leveldb" % "leveldb" % "0.12"
  lazy val leveldbIni = "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8"

  lazy val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
  lazy val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"

  lazy val twitterChill = "com.twitter" %% "chill-akka" % "0.9.5"

  lazy val persistence: Seq[ModuleID] = Seq(leveldb, leveldbIni)
  lazy val http: Seq[ModuleID] = Seq(sprayJson)

  lazy val akka: Seq[ModuleID] = Seq(akkaActor, akkaStream, akkaPersistence, akkaHttp) ++ persistence ++ http
  lazy val logging: Seq[ModuleID] = Seq(logback, scalaLogging)
  lazy val serialization: Seq[ModuleID] = Seq(twitterChill)
}
