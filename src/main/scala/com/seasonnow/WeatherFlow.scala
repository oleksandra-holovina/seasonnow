package com.seasonnow

import akka.NotUsed
import akka.stream.scaladsl.Flow
import com.seasonnow.data.SeasonData.Season.Season
import com.seasonnow.data.SeasonData.{Season, SeasonInfo}
import com.seasonnow.data.WeatherData.WeatherInfo
import com.typesafe.scalalogging.StrictLogging

object WeatherFlow extends StrictLogging {

  def weatherToSeasonFlow(): Flow[WeatherInfo, SeasonInfo, NotUsed] =
    Flow.apply[WeatherInfo]
      .map(info => (info.main.temp, coordToZipcode(info), determineSeason(info)))
      .map(tuple => SeasonInfo(tuple._1, tuple._2, tuple._3))

  private def coordToZipcode(weatherInfo: WeatherInfo): Option[String] = {
    val coord = weatherInfo.coord
    if (coord.lat == 41.85 && coord.lon == -87.65) {
      Some("60608")
    } else {
      logger.info(s"Coordinates changed $coord")
      None
    }
  }

  private def determineSeason(weatherInfo: WeatherInfo): Season = {
    weatherInfo.main.temp match {
      case temp if temp < 48 => Season.WINTER
      case temp if temp >= 48 && temp < 61 => Season.FALL
      case temp if temp >= 61 && temp < 67 => Season.SPRING
      case temp if temp >= 67 => Season.SUMMER
    }
  }
}