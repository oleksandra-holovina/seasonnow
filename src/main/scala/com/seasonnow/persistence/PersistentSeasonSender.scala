package com.seasonnow.persistence

import java.time.LocalDateTime

import akka.actor.typed.Behavior
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, RetentionCriteria}
import com.seasonnow.Settings
import com.seasonnow.api.twitter.TweetSender
import com.seasonnow.data.SeasonData.{Season, SeasonInfo}
import com.seasonnow.data.SeasonData.Season.Season
import com.seasonnow.persistence.SeasonSendingProtocol._
import com.typesafe.scalalogging.StrictLogging

case class PersistentSeasonSender(tweetSender: TweetSender, settings: Settings = Settings()) extends StrictLogging {

  def behavior(): Behavior[Command] =
    EventSourcedBehavior[Command, Event, State](
      PersistenceId.ofUniqueId("persistent-season"),
      State(),
      (state, command) => commandHandler(state, command),
      (state, event) => eventHandler(state, event)
    ).withRetention(
      RetentionCriteria.snapshotEvery(settings.snapshotFrequency, settings.snapshotAmount)
        .withDeleteEventsOnSnapshot
    )

  private def commandHandler(state: State, command: Command): Effect[Event, State] =
    command match {
      case UpdateSeason(seasonInfo) =>
        if (state.season != seasonInfo.season) {
          tweetSender.postSeasonUpdate(seasonInfo, state.seasonDetails.get(seasonInfo.season).map(_.lastOccurred))
        } else {
          logger.info(s"Same season $seasonInfo")
        }
        Effect.persist(tweetAndPersist(seasonInfo, state))
    }

  private def tweetAndPersist(seasonInfo: SeasonInfo, state: State): Seq[Event] = {
    val allSeasonsEvent = if (haveAllSeasonsBeenToday(state.seasonDetails, state.allSeasonsPostCreated)) {
      tweetSender.postAllSeasonsStatus(state.seasonDetails)
      Seq(AllSeasonsTodayPosted(now()))
    } else {
      Seq.empty
    }
    Seq(SeasonUpdated(seasonInfo.season, SeasonStateDetails(seasonInfo.temp, now()))) ++ allSeasonsEvent
  }

  private def haveAllSeasonsBeenToday(seasonMap: Map[Season, SeasonStateDetails], allSeasonPostedAt: Option[LocalDateTime]): Boolean = {
    val today = now().toLocalDate
    val isAfterPost: Season => Option[SeasonStateDetails] = season => seasonMap.get(season)
      .filter(details => details.lastOccurred.toLocalDate == today && (allSeasonPostedAt.isEmpty || details.lastOccurred.isAfter(allSeasonPostedAt.get)))

    Seq(
      isAfterPost(Season.FALL),
      isAfterPost(Season.SUMMER),
      isAfterPost(Season.SPRING),
      isAfterPost(Season.WINTER)
    ).forall(_.isDefined)
  }

  private def eventHandler(state: State, event: Event): State =
    event match {
      case SeasonUpdated(season, seasonDetails) => state.copy(season, state.seasonDetails.updated(season, seasonDetails))
      case AllSeasonsTodayPosted(now) => state.copy(allSeasonsPostCreated = Some(now))
    }

  private[persistence] def now(): LocalDateTime = LocalDateTime.now()
}
