package com.seasonnow

import akka.actor.typed.ActorRef

object WeatherProtocol {

  sealed trait WeatherCommand
  final case class FetchWeather(replyTo: ActorRef[WeatherReply]) extends WeatherCommand

  sealed trait WeatherReply
  final case class WeatherInfo(temperature: Double) extends WeatherReply
  final case class WeatherFetchFailed(exception: Throwable) extends WeatherReply
}
