package com.seasonnow

import com.seasonnow.Season.Season

object TweetSender {
  case class TweetData(text: String)

  def send(season: Season): Unit = {
    val tweetData = TweetData(season.toString)
    println(s"sending tweet $tweetData")
  }
}
