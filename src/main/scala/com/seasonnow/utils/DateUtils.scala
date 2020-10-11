package com.seasonnow.utils

import java.time.{Duration, LocalDateTime, Period}

object DateUtils {

  def calculateLastSeen(lastSeen: LocalDateTime, now: LocalDateTime): String = {
    val period = Period.between(lastSeen.toLocalDate, now.toLocalDate)
    val duration = Duration.between(lastSeen, now)

    val years = period.getYears
    val months = period.getMonths
    val days = period.getDays
    val hours = duration.toHours
    val minutes = duration.toMinutes

    getDifferenceWithUnits(years, "year")
      .getOrElse(getDifferenceWithUnits(months, "month")
      .getOrElse(getDifferenceWithUnits(days, "day")
      .getOrElse(getDifferenceWithUnits(hours, "hour")
      .getOrElse(getDifferenceWithUnits(minutes, "minute")
      .getOrElse("not so long")))))
  }

  private def getDifferenceWithUnits(diff: Long, unit: String): Option[String] = {
    if (diff <= 0) None
    else if (diff == 1) Some(s"$diff $unit")
    else Some(s"$diff ${unit}s")
  }
}
