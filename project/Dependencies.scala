import sbt._

object Dependencies {
  val akkaVersion = "2.6.9"
  val akkaHttpVersion = "10.2.1"
  val scalaTestVersion = "3.2.0"
  val slickVersion = "3.3.2"

  val Test = "test"

  lazy val akkaActor = "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion
  lazy val akkaStream = "com.typesafe.akka" %% "akka-stream-typed" % akkaVersion
  lazy val akkaPersistence = "com.typesafe.akka" %% "akka-persistence-typed" % akkaVersion
  lazy val akkaHttp = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion

  lazy val sprayJson = "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion

  lazy val leveldb = "org.iq80.leveldb" % "leveldb" % "0.12"
  lazy val leveldbIni = "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8"

  lazy val akkaPersistenceJdbc = "com.lightbend.akka" %% "akka-persistence-jdbc" % "4.0.0"
  lazy val slick = "com.typesafe.slick" %% "slick" % slickVersion
  lazy val slickHikaricp = "com.typesafe.slick" %% "slick-hikaricp" % slickVersion
  lazy val postgres = "org.postgresql" % "postgresql" % "42.2.17"

  lazy val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
  lazy val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"

  lazy val twitterChill = "com.twitter" %% "chill-akka" % "0.9.5"
  lazy val twitterApi = "com.danielasfregola" %% "twitter4s" % "7.0"

  lazy val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion
  lazy val wordSpec = "org.scalatest" %% "scalatest-wordspec" % scalaTestVersion
  lazy val mockito = "org.mockito" %% "mockito-scala" % "1.16.0"
  lazy val akkaTest = "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion
  lazy val akkaPersistenceTest = "com.typesafe.akka" %% "akka-persistence-testkit" % akkaVersion

  lazy val persistence: Seq[ModuleID] = Seq(leveldb, leveldbIni, akkaPersistenceJdbc, slick, slickHikaricp, postgres)
  lazy val http: Seq[ModuleID] = Seq(sprayJson, twitterApi)

  lazy val akka: Seq[ModuleID] = Seq(akkaActor, akkaStream, akkaPersistence, akkaHttp) ++ persistence ++ http
  lazy val logging: Seq[ModuleID] = Seq(logback, scalaLogging)
  lazy val serialization: Seq[ModuleID] = Seq(twitterChill)
  lazy val commonTest: Seq[ModuleID] = Seq(
    scalaTest % Test,
    wordSpec % Test,
    mockito % Test,
    akkaTest % Test,
    akkaPersistenceTest % Test
  )
}
