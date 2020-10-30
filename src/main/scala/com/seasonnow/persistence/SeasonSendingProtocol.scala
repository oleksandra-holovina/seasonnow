package com.seasonnow.persistence

import java.time.LocalDateTime

import com.seasonnow.data.SeasonData.Season.Season
import com.seasonnow.data.SeasonData.{Season, SeasonInfo}

object SeasonSendingProtocol {
  sealed trait Command
  final case class UpdateSeason(seasonInfo: SeasonInfo) extends Command

  sealed trait Event
  final case class SeasonUpdated(season: Season, seasonLastSeen: SeasonStateDetails) extends Event
  final case class AllSeasonsTodayPosted(now: LocalDateTime) extends Event

  final case class SeasonStateDetails(temp: Double, lastOccurred: LocalDateTime)
  final case class State(season: Season = Season.NOT_FETCHED, seasonDetails: Map[Season, SeasonStateDetails] = Map.empty, allSeasonsPostCreated: Option[LocalDateTime] = None)
}
