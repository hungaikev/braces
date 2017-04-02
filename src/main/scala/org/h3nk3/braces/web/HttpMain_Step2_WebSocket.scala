package org.h3nk3.braces.web

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink}
import akka.util.Timeout
import org.h3nk3.braces.backend.DroneConnectionHub
import org.h3nk3.braces.domain.Domain

import scala.concurrent.duration._

object HttpMain_Step2_WebSocket extends App 
  with Directives with OurOwnWebSocketSupport {
  
  import org.h3nk3.braces.domain.JsonDomain._
  
  implicit val system = ActorSystem("HttpApp")
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher = system.dispatcher
  implicit val timeout = Timeout(1.second)

  val log = Logging(system, getClass)
  
  Http().bindAndHandle(routes, "127.0.0.1", 8080)

  val droneClientConnectionHub = system.actorOf(DroneConnectionHub.proxyProps(system), DroneConnectionHub.name)
  
  // format: OFF
  def routes =
    path("drone" / DroneId) { droneId =>
      log.info("Accepted websocket connection from Drone: [{}]", droneId)
      val reply = droneClientConnectionHub ? DroneConnectionHub.DroneArrive(droneId)
      onSuccess(reply.mapTo[DroneConnectionHub.DroneHandler]) { handler =>
        handleWebSocketMessages(handler.flow)
      }
      
//      // handling it in line would be as simple as:
//      handleWebSocketMessages(
//        CoupledTerminationFlow.fromSinkAndSource(
//          in = Flow[Message]
//                .via(conversion)
//                .throttle(1, per = 1.second, maximumBurst = 1, mode = ThrottleMode.Shaping)
//                .to(ingestionHub),
//          out = Source.maybe[Message]
//        )
//      )
    }
  // format: ON
  
  def DroneId = Segment
  
  def conversion: Flow[Message, Domain.DroneData, Any] =
    Flow[Message].flatMapConcat(_.asBinaryMessage.getStreamedData)
      .mapAsync(1)(bs => Unmarshal(bs).to[Domain.DroneData])
}
