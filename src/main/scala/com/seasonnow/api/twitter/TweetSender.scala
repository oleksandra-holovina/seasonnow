package com.seasonnow.api.twitter

import java.time.LocalDateTime

import com.seasonnow.data.SeasonData.SeasonInfo

trait TweetSender {
  def postAllSeasonsStatus(): Unit
  def postSeasonUpdate(seasonInfo: SeasonInfo, lastSeen: Option[LocalDateTime]): Unit
}
