package com.seasonnow.persistence

import java.time.LocalDateTime

import com.seasonnow.data.SeasonData.Season.Season
import com.seasonnow.data.SeasonData.{Season, SeasonInfo}

object SeasonSendingProtocol {
  sealed trait Command
  final case class UpdateSeason(seasonInfo: SeasonInfo) extends Command

  sealed trait Event
  final case class SeasonUpdated(season: Season, seasonLastSeen: LocalDateTime) extends Event

  final case class State(season: Season = Season.NOT_FETCHED, seasonLastSeen: Map[Season, LocalDateTime] = Map.empty)
}
