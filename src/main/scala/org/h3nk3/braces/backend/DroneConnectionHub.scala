package org.h3nk3.braces.backend

import akka.NotUsed
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.actor.Actor.Receive
import akka.cluster.singleton.{ClusterSingletonProxy, ClusterSingletonProxySettings}
import akka.http.scaladsl.model.ws
import akka.http.scaladsl.model.ws.Message
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{CoupledTerminationFlow, Flow, Keep, Sink, Source, SourceQueueWithComplete}
import org.h3nk3.braces.backend.DroneActor.InitDrone
import org.h3nk3.braces.backend.DroneConnectionHub.{DroneArrive, DroneAway, DroneHandler, SendCommand}
import org.h3nk3.braces.domain.Domain
import org.h3nk3.braces.domain.Domain.DroneData
import org.reactivestreams.Publisher

object DroneConnectionHub {
  
  def proxyProps(system: ActorSystem) =
    ClusterSingletonProxy.props(s"/user/$name", ClusterSingletonProxySettings(system))
  
  def name = "droneClientConnectionHub"
  
  def props(): Props =
    Props(new DroneConnectionHub)
  
  case class SendCommand(id: String, command: Domain.DroneClientCommand)
  
  case class DroneArrive(id: String)
  case class DroneAway(id: String)
  
  /** Once registered here, we are able to handle and send messages to the field-deployed Drone */
  case class DroneHandler(flow: Flow[ws.Message, ws.Message, _])
}

/** Manages connections and pushes commands to field-deployed DroneClients */
class DroneConnectionHub extends Actor with ActorLogging {
  import DroneConnectionHub._
  
  implicit val mat = ActorMaterializer()
  
  var droneClientOut = Map.empty[String, SourceQueueWithComplete[Domain.DroneClientCommand]]
  
  override def receive: Receive = {
    case DroneArrive(droneId) =>
      val (toDroneInputQueue, toDronePublisher) =
        Source.queue[Domain.DroneClientCommand](32, OverflowStrategy.backpressure)
          .map(_ => ???.asInstanceOf[ws.Message])
        .toMat(Sink.asPublisher(false))(Keep.both).run()
      
      val outToDrone: Source[Message, NotUsed] = Source.fromPublisher(toDronePublisher)
      
      val inFromDrone = Flow[ws.Message]
        .map(msg => ???.asInstanceOf[DroneData])
        .map(data => droneId -> data)
        .to(Sink.actorRef(self, onCompleteMessage = DroneAway(droneId)))
      
      droneClientOut = droneClientOut.updated(droneId, toDroneInputQueue)
      
      sender() ! DroneHandler(
        CoupledTerminationFlow.fromSinkAndSource(inFromDrone, outToDrone)
      )
      
    case SendCommand(droneId, command) =>
      droneClientOut.get(droneId) match {
        case Some(queue) => //if queue.offer(command) => // push successful
        case _ => log.warning("Unable to push command {} to drone [{}]!!!", command, droneId)
      }
  }
}
