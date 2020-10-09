package com.seasonnow

import akka.actor.typed.Behavior
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, RetentionCriteria}
import com.seasonnow.Season.Season
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging

object PersistentSeasonSender extends StrictLogging {

  sealed trait Command
  final case class UpdateSeason(season: Season) extends Command

  sealed trait Event
  final case class SeasonUpdated(season: Season) extends Event

  final case class State(season: Season)

  def apply(tweetSender: TweetSender, settings: Settings = Settings(ConfigFactory.load())): Behavior[Command] =
    EventSourcedBehavior[Command, Event, State](
      persistenceId = PersistenceId.ofUniqueId("persistent-season"),
      emptyState = State(Season.NOT_FETCHED),
      commandHandler = commandHandler,
      eventHandler = eventHandler(tweetSender)
    ).withRetention(RetentionCriteria.snapshotEvery(settings.snapshotFrequency, settings.snapshotAmount))

  private val commandHandler: (State, Command) => Effect[Event, State] = (state, command) => command match {
    case UpdateSeason(season) if state.season != season =>
      logger.info(s"State was (${state.season}), season now ($season)")
      Effect.persist(SeasonUpdated(season))
    case UpdateSeason(season) =>
      logger.info(s"Same season $season")
      Effect.none
  }

  private def eventHandler(tweetSender: TweetSender): (State, Event) => State = (state, event) => event match {
    case SeasonUpdated(season) =>
      tweetSender.send(season)
      state.copy(season)
  }
}
