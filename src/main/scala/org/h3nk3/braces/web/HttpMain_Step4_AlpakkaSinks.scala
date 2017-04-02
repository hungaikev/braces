package org.h3nk3.braces.web

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{CoupledTerminationFlow, Flow, Sink, Source}
import org.h3nk3.braces.domain.Domain._

object HttpMain_Step4_AlpakkaSinks extends App 
  with Directives with OurOwnWebSocketSupport 
  with DroneInfoIngestion {
  
  implicit val system = ActorSystem("HttpApp")
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher = system.dispatcher

  val log = Logging(system, getClass)
  
  Http().bindAndHandle(routes, "127.0.0.1", 8080)

  initIngestionHub(Sink.ignore)

  // format: OFF
  def routes =
    path("drone" / DroneId) { droneId =>
      log.info("Accepted websocket connection from Drone: [{}]", droneId)
      handleWebSocketMessages(
        CoupledTerminationFlow.fromSinkAndSource(
          in = Flow[Message].via(conversion).to(ingestionHub),
          out = Source.maybe[Message]
        )
      )
    }
  // format: ON
  
  def DroneId = Segment
  
  def conversion: Flow[Message, DroneInfo, Any] =
    Flow[Message].flatMapConcat(_.asBinaryMessage.getStreamedData)
      .mapAsync(1)(t => Unmarshal(t).to[DroneInfo])
}