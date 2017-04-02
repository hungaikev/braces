package org.h3nk3.braces.web

import akka.actor.{ActorSystem, PoisonPill}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{CoupledTerminationFlow, Flow, Sink, Source}
import org.h3nk3.braces.backend.DroneActor
import org.h3nk3.braces.domain.Domain
import org.h3nk3.braces.domain.JsonDomain._

object HttpMain_Step3_ClusterSharding extends App 
  with Directives with OurOwnWebSocketSupport {
  
  implicit val system = ActorSystem("BracesBackend")
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher = system.dispatcher

  val log = Logging(system, getClass)
  
  Http().bindAndHandle(routes, "127.0.0.1", 8080)

  val drone = ClusterSharding(system).start(
    typeName = DroneActor.DroneName,
    entityProps = DroneActor.props(),
    settings = ClusterShardingSettings(system),
    extractEntityId = DroneActor.extractEntityId,
    extractShardId = DroneActor.extractShardId
  )
  

  // format: OFF
  def routes =
    path("drone" / DroneId) { droneId =>
      log.info("Accepted websocket connection from Drone: [{}]", droneId)
      handleWebSocketMessages(
        CoupledTerminationFlow.fromSinkAndSource(
          in = Flow[Message].via(conversion).to(Sink.actorRef(drone, onCompleteMessage = PoisonPill)),
          out = Source.maybe[Message]
        )
      )
    }
  // format: ON
  
  def DroneId = Segment
  
  def conversion: Flow[Message, Domain.DroneData, Any] =
    Flow[Message].flatMapConcat(_.asBinaryMessage.getStreamedData)
      .mapAsync(1)(t => Unmarshal(t).to[Domain.DroneData])
}
