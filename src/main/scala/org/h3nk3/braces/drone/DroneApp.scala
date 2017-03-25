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
import org.h3nk3.braces.domain.Domain._
import org.h3nk3.braces.domain.OurDomainJsonSupport

import scala.concurrent.duration._

object DroneApp extends App with OurDomainJsonSupport {

  val droneId = ThreadLocalRandom.current().nextInt(10)

  implicit val sys = ActorSystem("DroneSystem-real-" + droneId)
  implicit val mat = ActorMaterializer()
  import sys.dispatcher


  Http().singleWebSocketRequest(
    WebSocketRequest(s"ws://127.0.0.1:8080/drone/$droneId"),
    clientFlow = emitPositionAndMetrics)


  def emitPositionAndMetrics: Flow[Message, Message, Any] =
    CoupledTerminationFlow.fromSinkAndSource(
      Sink.ignore,
      Source.tick(initialDelay = 1.second, interval = 1.second, tick = NotUsed)
        .map(_ => getCurrentPosition)
        .via(renderAsJson)
        .map(TextMessage(_))
    )


  def getCurrentPosition: DronePosition =
    DronePosition(13, 37) // TODO make it move
  
  val renderAsJson: Flow[DronePosition, String, NotUsed] =
    Flow[DronePosition]
      .mapAsync(parallelism = 1)(p => Marshal(p).to[HttpEntity].flatMap(_.toStrict(1.seconds)))
      .map(_.data.utf8String)
  
}
