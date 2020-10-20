package com.seasonnow.api.twitter

import java.time.LocalDateTime

import com.seasonnow.data.SeasonData.SeasonInfo
import com.typesafe.scalalogging.StrictLogging

case class LocalTweetSender() extends TweetSender with StrictLogging {

  def postAllSeasonsStatus(): Unit =
    logger.info("All 4 seasons")

  def postSeasonUpdate(seasonInfo: SeasonInfo, lastSeen: Option[LocalDateTime]): Unit =
    logger.info(s"Season updated to $seasonInfo")
}
