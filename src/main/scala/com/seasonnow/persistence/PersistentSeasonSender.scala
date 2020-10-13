package com.seasonnow.persistence

import java.time.LocalDateTime

import akka.actor.typed.Behavior
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, RetentionCriteria}
import com.seasonnow.Settings
import com.seasonnow.api.TweetSender
import com.seasonnow.persistence.SeasonSendingProtocol._
import com.typesafe.scalalogging.StrictLogging

case class PersistentSeasonSender(tweetSender: TweetSender, settings: Settings = Settings()) extends StrictLogging {

  def behavior(): Behavior[Command] =
    EventSourcedBehavior[Command, Event, State](
      PersistenceId.ofUniqueId("persistent-season"),
      State(),
      (state, command) => commandHandler(state, command, tweetSender),
      (state, event) => eventHandler(state, event)
    ).withRetention(
      RetentionCriteria.snapshotEvery(settings.snapshotFrequency, settings.snapshotAmount)
        .withDeleteEventsOnSnapshot
    )

  private def commandHandler(state: State, command: Command, tweetSender: TweetSender): Effect[Event, State] =
    command match {
      case UpdateSeason(seasonInfo) if state.season != seasonInfo.season =>
        logger.info(s"Season was (${state.season}), season now (${seasonInfo.season})")
        Effect.persist(SeasonUpdated(seasonInfo.season, now()))
          .thenRun(_ => tweetSender.send(seasonInfo, state.seasonLastSeen.get(seasonInfo.season)))

      case UpdateSeason(seasonInfo) =>
        logger.info(s"Same season $seasonInfo")
        Effect.persist(SeasonUpdated(seasonInfo.season, now()))
    }

  private def eventHandler(state: State, event: Event): State =
    event match {
      case SeasonUpdated(season, lastSeen) => state.copy(season, state.seasonLastSeen.updated(season, lastSeen))
    }

  private[persistence] def now(): LocalDateTime = LocalDateTime.now()
}
