package com.seasonnow

import akka.actor.typed.ActorSystem
import akka.actor.typed.SpawnProtocol.Command
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.typesafe.config.ConfigFactory
import spray.json.DefaultJsonProtocol

import scala.concurrent.{ExecutionContextExecutor, Future}

case class MainInfo(temp: Double)

case class WeatherInfo(main: MainInfo)

object WeatherJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val mainFormat = jsonFormat1(MainInfo)
  implicit val weatherFormat = jsonFormat1(WeatherInfo)
}

object WeatherApiClient {

  import WeatherJsonSupport._

  def fetchCurrentTemperature(settings: Settings = Settings(ConfigFactory.load()))
                             (implicit actorSystem: ActorSystem[Command], ec: ExecutionContextExecutor): Future[Double] = {
    val chicagoId = 4887398
    val path = s"https://api.openweathermap.org/data/2.5/weather?id=$chicagoId&appid=${settings.weatherApiKey}&units=imperial"

    Http()
      .singleRequest(Get(path))
      .flatMap(extractResponse(_))
  }

  private def extractResponse(response: HttpResponse)(implicit actorSystem: ActorSystem[Command], ec: ExecutionContextExecutor): Future[Double] = {
    Unmarshal(response)
      .to[WeatherInfo]
      .map(_.main.temp)
  }
}
