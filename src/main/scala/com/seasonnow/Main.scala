package com.seasonnow

import akka.Done
import akka.actor.typed.{ActorSystem, SpawnProtocol}
import akka.stream.scaladsl.Source
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object Main extends App {
  Main(ActorSystem(SpawnProtocol(), "Main"))
}

case object Fetch
case class Main(actorSystem: ActorSystem[SpawnProtocol.Command], config: Config = ConfigFactory.load()) {

  implicit val sys: ActorSystem[SpawnProtocol.Command] = actorSystem
  implicit val ec: ExecutionContextExecutor = sys.executionContext

  val settings: Settings = Settings(config)

  val result: Future[Done] = Source.tick(0.second, settings.weatherFetchFrequency, Fetch)
    .mapAsync(1) { _ =>
      implicit val timeout: Timeout = 3.seconds
      WeatherFetcher.fetchWeather()
    }
    .via(WeatherFlow.weatherToSeasonFlow())
    .runForeach(TweetSender.send)

  result.onComplete {
    case Success(done) => println(s"Completed: $done")
    case Failure(ex)   => println(s"Failed: ${ex}")
  }
}
