package com.seasonnow.api.twitter

import java.time.{LocalDateTime, Period}

import com.danielasfregola.twitter4s.TwitterRestClient
import com.seasonnow.Settings
import com.seasonnow.data.SeasonData.Season.Season
import com.seasonnow.data.SeasonData.{Season, SeasonInfo}
import com.seasonnow.persistence.SeasonSendingProtocol.SeasonStateDetails
import com.seasonnow.utils.DateUtils
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

case class DefaultTweetSender(twitterClient: TwitterRestClient, settings: Settings = Settings())(implicit ec: ExecutionContextExecutor)
  extends TweetSender with StrictLogging {

  override def postAllSeasonsStatus(seasonsLastSeen: Map[Season, SeasonStateDetails]): Unit = {
    def temperatureAt(season: Season): String = {
      val details = seasonsLastSeen.get(season)
      val temp = details.map(_.temp).get
      val time = details.map(time => DateUtils.formatTime(time.lastOccurred)).get
      s"$temp at $time"
    }

    logger.info("All 4 seasons in a day")

    val tempForSeasons =
      s"""It was ${temperatureAt(Season.WINTER)},
         |and ${temperatureAt(Season.SPRING)},
         |and ${temperatureAt(Season.SUMMER)},
         |and ${temperatureAt(Season.FALL)}"""
        .stripMargin.replace("\n", " ")

    postTweet(status = s"Chicago experienced all 4 seasons today! $tempForSeasons.")
  }

  override def postSeasonUpdate(seasonInfo: SeasonInfo, lastSeen: Option[LocalDateTime]): Unit = {
    logger.info(s"Posting season ($seasonInfo) to twitter")

    val lastSeenText = lastSeen
      .filter(dateTime => Period.between(dateTime.toLocalDate, now().toLocalDate).getDays >= settings.lastSeenAfter)
      .map(time => s"\nThe last time it was ${seasonInfo.season} was ${DateUtils.calculateLastSeen(time, now())} ago.")
      .getOrElse("")

    val zipcodeText = seasonInfo.zipcode
      .map(zipcode => s" at $zipcode zipcode")
      .getOrElse("")

    postTweet(status = s"It is now ${seasonInfo.season} in Chicago (${seasonInfo.temp}F$zipcodeText).$lastSeenText")
  }

  private def postTweet(status: String): Unit = {
    val tweetFuture = twitterClient.createTweet(status = status)
    tweetFuture.onComplete {
      case Success(value) => logger.info(s"Tweet (${value.text}) was successfully posted")
      case Failure(exception) => logger.error("Couldn't post a tweet", exception)
    }
  }

  private[api] def now(): LocalDateTime = LocalDateTime.now()
}
