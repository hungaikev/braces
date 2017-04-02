package org.h3nk3.braces.drone

import java.util.concurrent.ThreadLocalRandom

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{CoupledTerminationFlow, Flow, Sink, Source}
import org.h3nk3.braces.backend.InputParser
import org.h3nk3.braces.domain.Domain._

import scala.concurrent.duration._

object DroneClient extends InputParser  {
  ???
}
