package com.seasonnow.api

import java.time.LocalDateTime

import com.danielasfregola.twitter4s.TwitterRestClient
import com.seasonnow.Settings
import com.seasonnow.data.SeasonData.SeasonInfo
import com.seasonnow.utils.DateUtils
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

case class TweetSender(twitterClient: TwitterRestClient, settings: Settings = Settings(ConfigFactory.load()))(implicit ec: ExecutionContextExecutor)
  extends StrictLogging {

  def send(seasonInfo: SeasonInfo, lastSeen: Option[LocalDateTime]): Unit = {
    logger.info(s"Posting season ($seasonInfo) to twitter")
    if (settings.env != "local") {
      val tweetFuture = twitterClient.createTweet(status = createStatus(seasonInfo, lastSeen))
      tweetFuture.onComplete {
        case Success(value) => logger.info(s"Tweet (${value.text}) was successfully posted")
        case Failure(exception) => logger.error("Couldn't post a tweet", exception)
      }
    }
  }

  private def createStatus(seasonInfo: SeasonInfo, lastSeen: Option[LocalDateTime]): String = {
    val lastSeenText = lastSeen
      .map(time => s"Last time this season was ${DateUtils.calculateLastSeen(time, now())} ago.")
      .getOrElse("")

    s"The season in Chicago has changed! It is ${seasonInfo.season} now (${seasonInfo.temp}F). $lastSeenText"
  }

  private[api] def now(): LocalDateTime = LocalDateTime.now()
}
