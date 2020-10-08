package com.seasonnow

import com.seasonnow.Season.Season

case class TweetData(text: String)

object TweetSender {
  def send(season: Season): Unit = {
    val tweetData = TweetData(season.toString)
    println(s"sending tweet $tweetData")
  }
}
