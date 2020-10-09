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

  val weatherFetchFrequency: FiniteDuration = Try(baseSettings.getDuration("weather-fetch-frequency"))
    .map(_.toScala)
    .getOrElse(defaultWeatherFetchFrequency)

  val weatherApiKey: String = baseSettings.getString("weather-api-key")

  val snapshotFrequency: Int = Try(baseSettings.getInt("snapshot-every")).getOrElse(defaultSnapshotFrequency)
  val snapshotAmount: Int = Try(baseSettings.getInt("keep-snapshot")).getOrElse(defaultSnapshotAmount)
}
