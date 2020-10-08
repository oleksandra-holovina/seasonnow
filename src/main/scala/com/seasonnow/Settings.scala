package com.seasonnow

import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration._
import scala.compat.java8.DurationConverters._
import scala.util.Try

object Settings {
  val defaultWeatherFetchFrequency: FiniteDuration = 1.minute
}

case class Settings(config: Config = ConfigFactory.load()) {
  import Settings._

  private val baseSettings = config.getConfig("com.seasonnow")
  val weatherFetchFrequency: FiniteDuration = Try(baseSettings.getDuration("weather.fetch.frequency"))
    .map(_.toScala)
    .getOrElse(defaultWeatherFetchFrequency)
}
