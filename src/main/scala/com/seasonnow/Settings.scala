package com.seasonnow

import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration._
import scala.compat.java8.DurationConverters._
import scala.util.Try

object Settings {
  val defaultWeatherFetchFrequency: FiniteDuration = 1.minute
  val defaultSnapshotFrequency = 10
  val defaultSnapshotAmount = 2
}

case class Settings(config: Config = ConfigFactory.load()) {
  import Settings._

  private val baseSettings = config.getConfig("com.seasonnow")

  val env: String = baseSettings.getString("env")
  val weatherFetchFrequency: FiniteDuration = Try(baseSettings.getDuration("weather-fetch-frequency"))
    .map(_.toScala)
    .getOrElse(defaultWeatherFetchFrequency)

  val weatherApiKey: String = baseSettings.getString("api.weather-api-key")

  val snapshotFrequency: Int = Try(baseSettings.getInt("persistence.snapshot-every")).getOrElse(defaultSnapshotFrequency)
  val snapshotAmount: Int = Try(baseSettings.getInt("persistence.keep-snapshot")).getOrElse(defaultSnapshotAmount)
}
