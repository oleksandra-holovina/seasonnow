package com.seasonnow.data

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

object WeatherData {

  case class Coordinates(lon: Double, lat: Double)
  case class MainInfo(temp: Double)
  case class WeatherInfo(coord: Coordinates, main: MainInfo)

  object WeatherJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
    implicit val coordFormat = jsonFormat2(Coordinates)
    implicit val mainFormat = jsonFormat1(MainInfo)
    implicit val weatherFormat = jsonFormat2(WeatherInfo)
  }
}
