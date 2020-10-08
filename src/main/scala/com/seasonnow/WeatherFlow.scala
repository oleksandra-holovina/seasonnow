package com.seasonnow

import akka.NotUsed
import akka.stream.scaladsl.Flow
import com.seasonnow.Season.Season
import com.seasonnow.WeatherProtocol.WeatherInfo

object Season extends Enumeration {
  type Season = Value

  val WINTER = Value("winter")
  val SPRING = Value("spring")
  val SUMMER = Value("summer")
  val FALL = Value("fall")
}

object WeatherFlow {

  def weatherToSeasonFlow(): Flow[WeatherInfo, Season, NotUsed] =
    Flow.apply[WeatherInfo]
      .map(_.temperature)
      .map {
        case it if it < 48 => Season.WINTER
        case it if it >= 48 && it < 61 => Season.FALL
        case it if it >= 61 && it < 67 => Season.SPRING
        case it if it >= 67 => Season.SUMMER
      }
}
