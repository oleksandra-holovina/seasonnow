package com.seasonnow.persistence

import java.time.LocalDateTime

import akka.actor.typed.Behavior
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, RetentionCriteria}
import com.seasonnow.Settings
import com.seasonnow.api.TweetSender
import com.seasonnow.data.SeasonData.Season
import com.seasonnow.persistence.SeasonSendingProtocol._
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging

case class PersistentSeasonSender(tweetSender: TweetSender, settings: Settings = Settings(ConfigFactory.load())) extends StrictLogging {

  def behavior(): Behavior[Command] =
    EventSourcedBehavior[Command, Event, State](
      persistenceId = PersistenceId.ofUniqueId("persistent-season"),
      emptyState = State(Season.NOT_FETCHED, Map.empty),
      commandHandler = commandHandler(tweetSender),
      eventHandler = eventHandler
    ).withRetention(RetentionCriteria.snapshotEvery(settings.snapshotFrequency, settings.snapshotAmount))

  private def commandHandler(tweetSender: TweetSender): (State, Command) => Effect[Event, State] = (state, command) => command match {
    case UpdateSeason(seasonInfo) if state.season != seasonInfo.season =>
      logger.info(s"Season was (${state.season}), season now (${seasonInfo.season})")
      Effect.persist[Event, State](SeasonUpdated(seasonInfo.season, now()))
        .thenRun(_ => tweetSender.send(seasonInfo, state.seasonLastSeen.get(seasonInfo.season)))

    case UpdateSeason(seasonInfo) =>
      logger.info(s"Same season $seasonInfo")
      Effect.persist[Event, State](SeasonUpdated(seasonInfo.season, now()))
  }

  private val eventHandler: (State, Event) => State = (state, event) => event match {
    case SeasonUpdated(season, lastSeen) => state.copy(season, state.seasonLastSeen.updated(season, lastSeen))
  }

  private[persistence] def now(): LocalDateTime = LocalDateTime.now()
}
