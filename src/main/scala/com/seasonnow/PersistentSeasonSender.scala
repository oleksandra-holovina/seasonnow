package com.seasonnow

import java.time.LocalDateTime

import akka.actor.typed.Behavior
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, RetentionCriteria}
import com.seasonnow.api.TweetSender
import com.seasonnow.data.SeasonData.Season.Season
import com.seasonnow.data.SeasonData.{Season, SeasonInfo}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging

object PersistentSeasonSender extends StrictLogging {

  sealed trait Command
  final case class UpdateSeason(seasonInfo: SeasonInfo) extends Command

  sealed trait Event
  final case class SeasonUpdated(season: Season, lastSeen: Option[LocalDateTime]) extends Event

  final case class State(season: Season, lastSeen: Option[LocalDateTime])

  def apply(tweetSender: TweetSender, settings: Settings = Settings(ConfigFactory.load())): Behavior[Command] =
    EventSourcedBehavior[Command, Event, State](
      persistenceId = PersistenceId.ofUniqueId("persistent-season"),
      emptyState = State(Season.NOT_FETCHED, None),
      commandHandler = commandHandler(tweetSender),
      eventHandler = eventHandler
    ).withRetention(RetentionCriteria.snapshotEvery(settings.snapshotFrequency, settings.snapshotAmount))

  private def commandHandler(tweetSender: TweetSender): (State, Command) => Effect[Event, State] = (state, command) => command match {
    case UpdateSeason(seasonInfo) if state.season != seasonInfo.season =>
      logger.info(s"State was (${state.season}), season now (${seasonInfo.season})")
      Effect.persist(SeasonUpdated(seasonInfo.season, Some(LocalDateTime.now())))
        .thenRun(_ => tweetSender.send(seasonInfo, state.lastSeen))
    case UpdateSeason(season) =>
      logger.info(s"Same season $season")
      Effect.none
  }

  private val eventHandler: (State, Event) => State = (state, event) => event match {
    case SeasonUpdated(season, lastSeen) => state.copy(season, lastSeen)
  }
}
