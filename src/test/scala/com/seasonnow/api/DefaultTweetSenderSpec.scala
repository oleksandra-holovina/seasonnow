package com.seasonnow.api

import java.time.{Instant, LocalDateTime}

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.entities.Tweet
import com.seasonnow.Settings
import com.seasonnow.api.twitter.DefaultTweetSender
import com.seasonnow.data.SeasonData.Season.Season
import com.seasonnow.data.SeasonData.{Season, SeasonInfo}
import com.seasonnow.persistence.SeasonSendingProtocol.SeasonStateDetails
import com.seasonnow.utils.DateUtils
import com.typesafe.config.ConfigFactory
import org.mockito.ArgumentMatchers.any
import org.mockito.{Mockito, MockitoSugar}
import org.scalatest.Outcome
import org.scalatest.wordspec.FixtureAnyWordSpecLike

import scala.concurrent.{ExecutionContextExecutor, Future}

object DefaultTweetSenderSpec {
  val temp: Double = 13.5
  val zipcode: String = "60601"
  val season: Season = Season.FALL
}

class DefaultTweetSenderSpec extends ScalaTestWithActorTestKit with FixtureAnyWordSpecLike with MockitoSugar {

  implicit val ec: ExecutionContextExecutor = testKit.internalSystem.executionContext

  import DefaultTweetSenderSpec._

  "Tweet Sender" should {
    "construct a status with last seen part " in { f =>
      val seasonInfo = SeasonInfo(temp, Some(zipcode), season)
      val lastSeen = f.now.minusDays(5)
      f.tweetSender.postSeasonUpdate(seasonInfo, Some(lastSeen))

      val status = s"It is now ${season.toString} in Chicago (${temp}F at $zipcode zipcode).\nThe last time it was ${season.toString} was 5 days ago."
      verify(f.twitterClient).createTweet(status = status)
    }

    "construct a status without last seen part due to close dates" in { f =>
      val seasonInfo = SeasonInfo(temp, Some(zipcode), season)
      val lastSeen = f.now.minusHours(2)
      f.tweetSender.postSeasonUpdate(seasonInfo, Some(lastSeen))

      val status = s"It is now ${season.toString} in Chicago (${temp}F at $zipcode zipcode)."
      verify(f.twitterClient).createTweet(status = status)
    }

    "construct a status without last seen part" in { f =>
      val seasonInfo = SeasonInfo(temp, Some(zipcode), season)
      f.tweetSender.postSeasonUpdate(seasonInfo, None)

      val status = s"It is now ${season.toString} in Chicago (${temp}F at $zipcode zipcode)."
      verify(f.twitterClient).createTweet(status = status)
    }

    "construct a status without zipcode" in { f =>
      val seasonInfo = SeasonInfo(temp, None, season)
      f.tweetSender.postSeasonUpdate(seasonInfo, None)

      val status = s"It is now ${season.toString} in Chicago (${temp}F)."
      verify(f.twitterClient).createTweet(status = status)
    }

    "construct all 4 seasons status" in { f =>
      val lastSeenMap = Map(
        Season.WINTER -> SeasonStateDetails(44, f.now.minusHours(1)),
        Season.SPRING -> SeasonStateDetails(55, f.now.minusHours(2)),
        Season.SUMMER -> SeasonStateDetails(88, f.now.minusHours(3)),
        Season.FALL -> SeasonStateDetails(53, f.now.minusHours(4)),
      )
      f.tweetSender.postAllSeasonsStatus(lastSeenMap)

      val status =
        s"""Chicago experienced all 4 seasons today! It was 44.0 at ${DateUtils.formatTime(f.now.minusHours(1))},
           |and 55.0 at ${DateUtils.formatTime(f.now.minusHours(2))},
           |and 88.0 at ${DateUtils.formatTime(f.now.minusHours(3))},
           |and 53.0 at ${DateUtils.formatTime(f.now.minusHours(4))}."""
          .stripMargin.replace("\n", " ")

      verify(f.twitterClient).createTweet(status = status)
    }
  }

  case class FixtureParam(twitterClient: TwitterRestClient, tweetSender: DefaultTweetSender, now: LocalDateTime)

  override protected def withFixture(test: OneArgTest): Outcome = {
    val twitterClient = mock[TwitterRestClient]
    Mockito.when(twitterClient.createTweet(any(), any(), any(), any(), any(), any(), any(), any(), any()))
      .thenReturn(Future.successful(Tweet(id = 1, id_str = "", text = "", source = "", created_at = Instant.EPOCH)))

    val tweetSender = spy(twitter.DefaultTweetSender(twitterClient, Settings(ConfigFactory.load())))

    val now = LocalDateTime.of(2020, 10, 10, 4, 20, 0)
    Mockito.when(tweetSender.now()).thenReturn(now)

    test(FixtureParam(twitterClient, tweetSender, now))
  }
}
