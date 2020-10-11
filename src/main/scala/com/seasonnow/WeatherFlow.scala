package com.seasonnow

import akka.NotUsed
import akka.stream.scaladsl.Flow
import com.seasonnow.data.SeasonData.{Season, SeasonInfo}
import com.seasonnow.data.WeatherData.WeatherInfo

object WeatherFlow {

  def weatherToSeasonFlow(): Flow[WeatherInfo, SeasonInfo, NotUsed] =
    Flow.apply[WeatherInfo]
      .map(_.main.temp)
      .map {
        case temp if temp < 48 => SeasonInfo(temp, "", Season.WINTER)
        case temp if temp >= 48 && temp < 61 => SeasonInfo(temp, "", Season.FALL)
        case temp if temp >= 61 && temp < 67 => SeasonInfo(temp, "", Season.SPRING)
        case temp if temp >= 67 => SeasonInfo(temp, "", Season.SUMMER)
      }
}