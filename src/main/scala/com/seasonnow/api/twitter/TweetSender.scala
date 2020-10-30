package com.seasonnow.api.twitter

import java.time.LocalDateTime

import com.seasonnow.data.SeasonData.Season.Season
import com.seasonnow.data.SeasonData.SeasonInfo
import com.seasonnow.persistence.SeasonSendingProtocol.SeasonStateDetails

trait TweetSender {
  def postAllSeasonsStatus(seasonsLastSeen: Map[Season, SeasonStateDetails]): Unit
  def postSeasonUpdate(seasonInfo: SeasonInfo, lastSeen: Option[LocalDateTime]): Unit
}
