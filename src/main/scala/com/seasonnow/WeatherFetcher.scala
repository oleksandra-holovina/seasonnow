package com.seasonnow

import com.seasonnow.WeatherProtocol.WeatherInfo

import scala.concurrent.Future
import scala.util.Random

object WeatherFetcher {
  def fetchWeather(): Future[WeatherInfo] =
    Future.successful(WeatherInfo(Random.nextDouble() * 100))
}
