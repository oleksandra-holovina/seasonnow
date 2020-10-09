package com.seasonnow

import akka.NotUsed
import akka.stream.scaladsl.Flow
import com.seasonnow.Season.Season

object Season extends Enumeration {
  type Season = Value

  val WINTER = Value("winter")
  val SPRING = Value("spring")
  val SUMMER = Value("summer")
  val FALL = Value("fall")
  val NOT_FETCHED = Value("unknown")
}

object WeatherFlow {

  def weatherToSeasonFlow(): Flow[Double, Season, NotUsed] =
    Flow.apply
      .map {
        case it if it < 48 => Season.WINTER
        case it if it >= 48 && it < 61 => Season.FALL
        case it if it >= 61 && it < 67 => Season.SPRING
        case it if it >= 67 => Season.SUMMER
      }
}
