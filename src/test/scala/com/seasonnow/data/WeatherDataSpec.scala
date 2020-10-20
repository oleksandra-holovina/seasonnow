package com.seasonnow.data

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.seasonnow.data.WeatherData.{Coordinates, MainInfo, WeatherInfo}
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.{Await, ExecutionContextExecutor}
import scala.concurrent.duration._
import com.seasonnow.data.WeatherData.WeatherJsonSupport._

class WeatherDataSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  implicit val ec: ExecutionContextExecutor = testKit.internalSystem.executionContext

  "Weather Data" should {
    "deserialize" in {
      val weatherInfo = Await.result(Unmarshal(
        """
          |{
          |"coord": {"lon": 33.99, "lat": 11.5},
          |"main": {"temp": 13.5}
          |}
          |""".stripMargin)
        .to[WeatherInfo], 3.seconds)
      weatherInfo shouldBe WeatherInfo(Coordinates(33.99, 11.5), MainInfo(13.5))
    }
  }

}
