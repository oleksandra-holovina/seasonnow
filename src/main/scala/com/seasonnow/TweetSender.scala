package com.seasonnow

import com.danielasfregola.twitter4s.TwitterRestClient
import com.seasonnow.Season.Season
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

case class TweetSender(twitterClient: TwitterRestClient, settings: Settings = Settings(ConfigFactory.load()))(implicit ec: ExecutionContextExecutor)
  extends StrictLogging {

  def send(season: Season): Unit = {
    logger.info(s"Posting season ($season) to twitter")
    if (settings.env != "local") {
      val tweetFuture = twitterClient.createTweet(status = season.toString)
      tweetFuture.onComplete {
        case Success(value) => logger.info(s"Tweet (${value.text}) was successully posted")
        case Failure(exception) => logger.error("Couldn't post a tweet", exception)
      }
    }
  }
}
