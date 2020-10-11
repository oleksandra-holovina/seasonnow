package com.seasonnow.persistence

import java.time.LocalDateTime

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import com.seasonnow.api.TweetSender
import com.seasonnow.data.SeasonData
import com.seasonnow.data.SeasonData.Season.Season
import com.seasonnow.data.SeasonData.{Season, SeasonInfo}
import com.seasonnow.persistence.SeasonSendingProtocol._
import com.typesafe.config.ConfigFactory
import org.mockito.ArgumentMatchers.any
import org.mockito.{Mockito, MockitoSugar}
import org.scalatest.Outcome
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.FixtureAnyWordSpecLike

object PersistentSeasonSenderSpec {
  val temp: Double = 15.8
  val season: SeasonData.Season.Value = Season.SPRING
  val newSeason: SeasonData.Season.Value = Season.FALL
  val zipcode: Option[String] = Some("60606")
}

class PersistentSeasonSenderSpec
  extends ScalaTestWithActorTestKit(EventSourcedBehaviorTestKit.config.withFallback(ConfigFactory.load()))
    with FixtureAnyWordSpecLike
    with MockitoSugar
    with Matchers {

  import PersistentSeasonSenderSpec._

  "Persistent Season Sender" should {
    val seasonInfo = SeasonInfo(temp, zipcode, season)

    "update season only when it's different than previous" in { f =>
      runAndVerify(f, seasonInfo, Map(season -> f.now1))

      reset(f.tweetSender)

      runAndVerify(f, seasonInfo.copy(temp = temp + 3), Map(season -> f.now2), shouldCallTwitter = false)
      runAndVerify(f, seasonInfo.copy(season = newSeason), Map(season -> f.now2, newSeason -> f.now2))

      reset(f.tweetSender)

      runAndVerify(f, seasonInfo, Map(season -> f.now2, newSeason -> f.now2), lastSeen = Some(f.now2))
    }
  }

  private def runAndVerify(
                            f: FixtureParam,
                            seasonInfo: SeasonInfo,
                            lastSeenSeasons: Map[Season, LocalDateTime],
                            lastSeen: Option[LocalDateTime] = None,
                            shouldCallTwitter: Boolean = true
                          ): Unit = {
    val result = f.testKit.runCommand(UpdateSeason(seasonInfo))
    result.state shouldBe State(seasonInfo.season, lastSeenSeasons)

    if (shouldCallTwitter) {
      verify(f.tweetSender).send(seasonInfo, lastSeen)
    } else {
      verify(f.tweetSender, never).send(any(), any())
    }
  }

  case class FixtureParam(testKit: EventSourcedBehaviorTestKit[Command, Event, State], tweetSender: TweetSender, now1: LocalDateTime, now2: LocalDateTime)

  override protected def withFixture(test: OneArgTest): Outcome = {
    val tweetSender = mock[TweetSender]
    val seasonSender = spy(PersistentSeasonSender(tweetSender))

    val now1 = LocalDateTime.of(2020, 10, 10, 13, 30, 0)
    val now2 = LocalDateTime.of(2020, 10, 10, 13, 45, 0)

    Mockito.when(seasonSender.now())
      .thenReturn(now1)
      .thenReturn(now2)

    val testKit = EventSourcedBehaviorTestKit[Command, Event, State](system, seasonSender.behavior())
    test(FixtureParam(testKit, tweetSender, now1, now2))
  }
}
