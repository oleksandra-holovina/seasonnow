package com.seasonnow.persistence

import java.time.LocalDateTime

import com.seasonnow.data.SeasonData.Season.Season
import com.seasonnow.data.SeasonData.SeasonInfo

object SeasonSendingProtocol {
  sealed trait Command extends Serializable
  final case class UpdateSeason(seasonInfo: SeasonInfo) extends Command

  sealed trait Event extends Serializable
  final case class SeasonUpdated(season: Season, seasonLastSeen: LocalDateTime) extends Event

  final case class State(season: Season, seasonLastSeen: Map[Season, LocalDateTime]) extends Serializable
}
