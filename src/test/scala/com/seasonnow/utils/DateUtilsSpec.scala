package com.seasonnow.utils

import java.time.LocalDateTime

import org.scalatest.Outcome
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.FixtureAnyWordSpecLike

class DateUtilsSpec extends FixtureAnyWordSpecLike with Matchers {

  "Date Utils" should {
    "have difference in years (more than 1)" in { f =>
      val lastSeen = f.now.minusYears(2)
      DateUtils.calculateLastSeen(lastSeen, f.now) shouldBe "2 years"
    }
    "have difference in 1 year" in { f =>
      val lastSeen = f.now.minusYears(1)
      DateUtils.calculateLastSeen(lastSeen, f.now) shouldBe "1 year"
    }
    "have difference in months" in { f =>
      val lastSeen = f.now.minusMonths(3)
      DateUtils.calculateLastSeen(lastSeen, f.now) shouldBe "3 months"
    }
    "have difference in days" in { f =>
      val lastSeen = f.now.minusDays(5)
      DateUtils.calculateLastSeen(lastSeen, f.now) shouldBe "5 days"
    }
    "have difference in hours" in { f =>
      val lastSeen = f.now.minusHours(7)
      DateUtils.calculateLastSeen(lastSeen, f.now) shouldBe "7 hours"
    }
    "have difference in minutes" in { f =>
      val lastSeen = f.now.minusMinutes(8)
      DateUtils.calculateLastSeen(lastSeen, f.now) shouldBe "8 minutes"
    }
    "format date" in { f =>
      DateUtils.formatTime(f.now) shouldBe "04:40 PM"
    }
  }

  case class FixtureParam(now: LocalDateTime)

  override protected def withFixture(test: OneArgTest): Outcome = {
    val now = LocalDateTime.of(2020, 10, 10, 16, 40, 0)
    test(FixtureParam(now))
  }
}
