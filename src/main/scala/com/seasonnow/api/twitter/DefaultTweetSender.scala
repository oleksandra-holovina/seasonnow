package com.seasonnow.api.twitter

import java.time.LocalDateTime

import com.danielasfregola.twitter4s.TwitterRestClient
import com.seasonnow.Settings
import com.seasonnow.data.SeasonData.SeasonInfo
import com.seasonnow.utils.DateUtils
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

case class DefaultTweetSender(twitterClient: TwitterRestClient, settings: Settings = Settings())(implicit ec: ExecutionContextExecutor)
  extends TweetSender with StrictLogging {

  override def postAllSeasonsStatus(): Unit = {
    logger.info("All 4 seasons in a day")
    postTweet("Yay! All 4 seasons in one day!")
  }

  override def postSeasonUpdate(seasonInfo: SeasonInfo, lastSeen: Option[LocalDateTime]): Unit = {
    logger.info(s"Posting season ($seasonInfo) to twitter")
    postTweet(createStatus(seasonInfo, lastSeen))
  }

  private def postTweet(status: String): Unit = {
    val tweetFuture = twitterClient.createTweet(status = status)
    tweetFuture.onComplete {
      case Success(value) => logger.info(s"Tweet (${value.text}) was successfully posted")
      case Failure(exception) => logger.error("Couldn't post a tweet", exception)
    }
  }

  private def createStatus(seasonInfo: SeasonInfo, lastSeen: Option[LocalDateTime]): String = {
    val lastSeenText = lastSeen
      .map(time => s" Last time this season was ${DateUtils.calculateLastSeen(time, now())} ago.")
      .getOrElse("")

    val zipcodeText = seasonInfo.zipcode
      .map(zipcode => s" at $zipcode zipcode")
      .getOrElse("")

    s"The season in Chicago has changed! It is ${seasonInfo.season} now (${seasonInfo.temp}F$zipcodeText).$lastSeenText"
  }

  private[api] def now(): LocalDateTime = LocalDateTime.now()
}
