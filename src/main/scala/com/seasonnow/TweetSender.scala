package com.seasonnow

import com.danielasfregola.twitter4s.TwitterRestClient
import com.seasonnow.Season.Season
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging

case class TweetSender(twitterClient: TwitterRestClient, settings: Settings = Settings(ConfigFactory.load())) extends StrictLogging {

  def send(season: Season): Unit = {
    logger.info(s"Posting season ($season) to twitter")
    if (settings.env != "local") {
      twitterClient.createTweet(status = season.toString)
    }
  }
}
