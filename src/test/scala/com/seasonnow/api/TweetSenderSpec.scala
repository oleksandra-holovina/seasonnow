package com.seasonnow.api

import java.time.{Instant, LocalDateTime}

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.entities.Tweet
import com.seasonnow.Settings
import com.seasonnow.data.SeasonData.Season.Season
import com.seasonnow.data.SeasonData.{Season, SeasonInfo}
import com.typesafe.config.ConfigFactory
import org.mockito.{Mockito, MockitoSugar}
import org.scalatest.Outcome
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.FixtureAnyWordSpecLike
import org.mockito.ArgumentMatchers.any

import scala.concurrent.{ExecutionContextExecutor, Future}

object TweetSenderSpec {
  val temp: Double = 13.5
  val zipcode: String = "60601"
  val season: Season = Season.FALL
}

class TweetSenderSpec extends ScalaTestWithActorTestKit with FixtureAnyWordSpecLike with MockitoSugar with Matchers {

  implicit val ec: ExecutionContextExecutor = testKit.internalSystem.executionContext

  import TweetSenderSpec._

  "Tweet Sender" should {
    "construct a status with last seen part" in { f =>
      val seasonInfo = SeasonInfo(temp, Some(zipcode), season)
      val lastSeen = f.now.minusHours(2)
      f.tweetSender.send(seasonInfo, Some(lastSeen))

      val status = s"The season in Chicago has changed! It is ${season.toString} now (${temp}F at $zipcode zipcode). Last time this season was 2 hours ago."
      verify(f.twitterClient).createTweet(status = status)
    }

    "construct a status without last seen part" in { f =>
      val seasonInfo = SeasonInfo(temp, Some(zipcode), season)
      f.tweetSender.send(seasonInfo, None)

      val status = s"The season in Chicago has changed! It is ${season.toString} now (${temp}F at $zipcode zipcode)."
      verify(f.twitterClient).createTweet(status = status)
    }

    "construct a status without zipcode" in { f =>
      val seasonInfo = SeasonInfo(temp, None, season)
      f.tweetSender.send(seasonInfo, None)

      val status = s"The season in Chicago has changed! It is ${season.toString} now (${temp}F)."
      verify(f.twitterClient).createTweet(status = status)
    }
  }

  case class FixtureParam(twitterClient: TwitterRestClient, tweetSender: TweetSender, now: LocalDateTime)

  override protected def withFixture(test: OneArgTest): Outcome = {
    val twitterClient = mock[TwitterRestClient]
    Mockito.when(twitterClient.createTweet(any(), any(), any(), any(), any(), any(), any(), any(), any()))
      .thenReturn(Future.successful(Tweet(id = 1, id_str = "", text = "", source = "", created_at = Instant.EPOCH)))

    val tweetSender = spy(TweetSender(twitterClient, Settings(ConfigFactory.load())))

    val now = LocalDateTime.of(2020, 10, 10, 4, 20, 0)
    Mockito.when(tweetSender.now()).thenReturn(now)

    test(FixtureParam(twitterClient, tweetSender, now))
  }
}
