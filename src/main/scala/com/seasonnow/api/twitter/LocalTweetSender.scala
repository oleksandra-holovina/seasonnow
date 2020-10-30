package com.seasonnow.api.twitter

import java.time.LocalDateTime

import com.seasonnow.data.SeasonData.Season.Season
import com.seasonnow.data.SeasonData.SeasonInfo
import com.seasonnow.persistence.SeasonSendingProtocol.SeasonStateDetails
import com.typesafe.scalalogging.StrictLogging

case class LocalTweetSender() extends TweetSender with StrictLogging {

  def postAllSeasonsStatus(seasonsLastSeen: Map[Season, SeasonStateDetails]): Unit =
    logger.info("All 4 seasons")

  def postSeasonUpdate(seasonInfo: SeasonInfo, lastSeen: Option[LocalDateTime]): Unit =
    logger.info(s"Season updated to $seasonInfo")
}
