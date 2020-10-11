package com.seasonnow

import akka.Done
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Props, SpawnProtocol, SupervisorStrategy}
import akka.stream.scaladsl.Source
import akka.util.Timeout
import com.danielasfregola.twitter4s.TwitterRestClient
import com.seasonnow.api.{TweetSender, WeatherApiClient}
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object Main extends App {
  Main()(ActorSystem(SpawnProtocol(), "Main"))
}

case object Fetch

case class Main(config: Config = ConfigFactory.load())(implicit val actorSystem: ActorSystem[SpawnProtocol.Command]) extends StrictLogging {
  implicit val ec: ExecutionContextExecutor = actorSystem.executionContext
  implicit val timeout: Timeout = Timeout(3.seconds)

  val settings: Settings = Settings(config)

  val tweetSender: TweetSender = api.TweetSender(TwitterRestClient())
  val seasonSenderBehavior: Behavior[PersistentSeasonSender.Command] = Behaviors.supervise(PersistentSeasonSender(tweetSender))
    .onFailure(SupervisorStrategy.resume)

  val seasonSenderSinkFuture: Future[ActorRef[PersistentSeasonSender.Command]] =
    actorSystem.ask[ActorRef[PersistentSeasonSender.Command]](replyTo => SpawnProtocol.Spawn(
      seasonSenderBehavior,
      "SeasonSenderSink",
      Props.empty,
      replyTo
    ))

  seasonSenderSinkFuture.onComplete {
    case Success(seasonSenderSink) => startStream(seasonSenderSink)
    case Failure(exception) => logger.error("Couldn't create season sender", exception)
  }

  private def startStream(seasonSenderSink: ActorRef[PersistentSeasonSender.Command]): Unit = {
    val result: Future[Done] = Source.tick(0.second, settings.weatherFetchFrequency, Fetch)
      .mapAsync(1) { _ => WeatherApiClient.fetchCurrentTemperature() }
      .via(WeatherFlow.weatherToSeasonFlow())
      .runForeach(seasonSenderSink ! PersistentSeasonSender.UpdateSeason(_))

    logger.info("Season Now initialized")

    result.onComplete {
      case Success(done) => logger.info(s"Main stream completed: $done")
      case Failure(ex) => logger.error(s"Main stream failed", ex)
    }
  }
}
