package com.seasonnow.persistence

import java.time.LocalDateTime

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import com.seasonnow.api.twitter.DefaultTweetSender
import com.seasonnow.data.SeasonData
import com.seasonnow.data.SeasonData.Season.Season
import com.seasonnow.data.SeasonData.{Season, SeasonInfo}
import com.seasonnow.persistence.SeasonSendingProtocol._
import com.typesafe.config.ConfigFactory
import org.mockito.ArgumentMatchers.any
import org.mockito.{Mockito, MockitoSugar}
import org.scalatest.Outcome
import org.scalatest.wordspec.FixtureAnyWordSpecLike

object PersistentSeasonSenderSpec {
  val temp: Double = 15.8
  val season: SeasonData.Season.Value = Season.FALL
  val newSeason: SeasonData.Season.Value = Season.SPRING
  val zipcode: Option[String] = Some("60606")

  val seasonInfo: SeasonInfo = SeasonInfo(temp, zipcode, season)
}

class PersistentSeasonSenderSpec
  extends ScalaTestWithActorTestKit(EventSourcedBehaviorTestKit.config.withFallback(ConfigFactory.load()))
    with FixtureAnyWordSpecLike
    with MockitoSugar {

  import PersistentSeasonSenderSpec._

  "Persistent Season Sender" should {

    "update season only when it's different than previous" in { f =>
      runAndVerify(f, seasonInfo, Map(season -> f.now))

      reset(f.tweetSender)

      val now2 = LocalDateTime.of(2020, 10, 10, 13, 45, 0)
      Mockito.when(f.seasonSender.now()).thenReturn(now2)

      runAndVerify(f, seasonInfo.copy(temp = temp + 3), Map(season -> now2), shouldCallTwitter = false)
      runAndVerify(f, seasonInfo.copy(season = newSeason), Map(season -> now2, newSeason -> now2))

      reset(f.tweetSender)

      runAndVerify(f, seasonInfo, Map(season -> now2, newSeason -> now2), lastSeen = Some(now2))
    }

    "send all 4 seasons message only once " in { f =>
      val seasonMap = runAndVerifyAllSeasons(f, f.now)
      runAndVerify(f, seasonInfo, seasonMap, allSeasonsPostCreated = Some(f.now)) //on next season update
      verify(f.tweetSender).postAllSeasonsStatus()

      reset(f.tweetSender)

      runAndVerify(f, seasonInfo.copy(season = newSeason), seasonMap, Some(f.now), Some(f.now))
      verify(f.tweetSender, never).postAllSeasonsStatus()
    }

    "not send all 4 seasons message for different dates" in { f =>
      val now2 = LocalDateTime.of(2020, 10, 9, 13, 30, 0)
      Mockito.when(f.seasonSender.now()).thenReturn(now2).thenReturn(now2).thenReturn(f.now)

      val seasonMap = runAndVerifyAllSeasons(f, now2)
      runAndVerify(f, seasonInfo, seasonMap)
      verify(f.tweetSender, never).postAllSeasonsStatus()
    }
  }

  private def runAndVerify(f: FixtureParam,
                           seasonInfo: SeasonInfo,
                           lastSeenSeasons: Map[Season, LocalDateTime],
                           lastSeen: Option[LocalDateTime] = None,
                           allSeasonsPostCreated: Option[LocalDateTime] = None,
                           shouldCallTwitter: Boolean = true
                          ): Unit = {
    val result = f.testKit.runCommand(UpdateSeason(seasonInfo))
    result.state shouldBe State(seasonInfo.season, lastSeenSeasons, allSeasonsPostCreated)

    if (shouldCallTwitter) {
      verify(f.tweetSender).postSeasonUpdate(seasonInfo, lastSeen)
    } else {
      verify(f.tweetSender, never).postSeasonUpdate(any(), any())
    }
  }

  private def runAndVerifyAllSeasons(f: FixtureParam, dateTime: LocalDateTime): Map[Season, LocalDateTime] = {
    def checkSeason(season: Season, seasonMap: Map[Season, LocalDateTime]): Unit = {
      runAndVerify(f, seasonInfo.copy(season = season), seasonMap)
      verify(f.tweetSender, never).postAllSeasonsStatus()
    }

    val seasonMap = Map(Season.WINTER -> dateTime)
    checkSeason(Season.WINTER, seasonMap)

    val seasonMap2 = seasonMap + (Season.SPRING -> f.now)
    checkSeason(Season.SPRING, seasonMap2)

    val seasonMap3 = seasonMap2 + (Season.SUMMER -> f.now)
    checkSeason(Season.SUMMER, seasonMap3)

    val seasonMap4 = seasonMap3 + (Season.FALL -> f.now)
    checkSeason(Season.FALL, seasonMap4)

    seasonMap4
  }

  case class FixtureParam(testKit: EventSourcedBehaviorTestKit[Command, Event, State], tweetSender: DefaultTweetSender, seasonSender: PersistentSeasonSender, now: LocalDateTime)

  override protected def withFixture(test: OneArgTest): Outcome = {
    val tweetSender = mock[DefaultTweetSender]
    val seasonSender = spy(PersistentSeasonSender(tweetSender))

    val now = LocalDateTime.of(2020, 10, 10, 13, 30, 0)
    Mockito.when(seasonSender.now()).thenReturn(now)

    val testKit = EventSourcedBehaviorTestKit[Command, Event, State](system, seasonSender.behavior())
    test(FixtureParam(testKit, tweetSender, seasonSender, now))
  }
}
