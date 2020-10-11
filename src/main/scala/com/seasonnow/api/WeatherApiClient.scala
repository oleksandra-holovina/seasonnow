package com.seasonnow.api

import akka.actor.typed.ActorSystem
import akka.actor.typed.SpawnProtocol.Command
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.seasonnow.Settings
import com.seasonnow.data.WeatherData.{Coordinates, MainInfo, WeatherInfo}
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.Random

object WeatherApiClient {

  import com.seasonnow.data.WeatherData.WeatherJsonSupport._

  def fetchCurrentTemperature(settings: Settings = Settings(ConfigFactory.load()))
                             (implicit actorSystem: ActorSystem[Command], ec: ExecutionContextExecutor): Future[WeatherInfo] = {
    if (settings.env == "local") {
      Future.successful(WeatherInfo(Coordinates(Random.nextDouble(), Random.nextDouble()), MainInfo(Random.nextDouble() * 100)))
    } else {
      callWeatherApi(settings)
    }
  }

  private def callWeatherApi(settings: Settings)(implicit actorSystem: ActorSystem[Command], ec: ExecutionContextExecutor): Future[WeatherInfo] = {
    val chicagoId = 4887398
    val path = s"https://api.openweathermap.org/data/2.5/weather?id=$chicagoId&appid=${settings.weatherApiKey}&units=imperial"

    Http()
      .singleRequest(Get(path))
      .flatMap(Unmarshal(_).to[WeatherInfo])
  }
}
