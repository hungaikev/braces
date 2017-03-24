package org.h3nk3.braces.web

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.{Directives, HttpApp, Route}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{CoupledTerminationFlow, Flow, Sink, Source}
import org.h3nk3.braces.backend.{DroneInfo, ServerCommand}

object HttpMain extends App 
  with Directives with OurOwnWebSocketSupport 
  with DroneInfoIngestion { 

  implicit val system = ActorSystem("HttpApp")
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher = system.dispatcher
  
  initIngestionHub(Sink.foreach { droneInfo => 
    println("GOT: " + droneInfo)
    // TODO replace with sending data to cluster,
    // - shardded since we have the drone's id
  })
  
  Http().bindAndHandle(routes, "127.0.0.1", 8080)

  def decodeDroneInfo: Flow[Message, DroneInfo, NotUsed] =
    Flow[Message].via(this.toStrictText).map(_ => DroneInfo("ID", null, 2, 1, 1)) // FIXME decoding

  def encodeServerCommand: Flow[ServerCommand, Message, NotUsed] =  
    Flow.fromFunction(_ => TextMessage("")) // TODO render commands
  
  // format: OFF
  def routes =
    pathSingleSlash {
      getFromResource("braces.html")
    } ~
    path("ws") {
      handleWebSocketMessages(websocketEcho)
    } ~ // TODO tell intellij about this trailing ~ thing
    path("ingest") {
      handleWebSocketMessages(
        CoupledTerminationFlow.fromSinkAndSource(
          Flow[Message].via(decodeDroneInfo).to(this.ingestionHub),
          Source.maybe.via(encodeServerCommand) // TODO issue commands?
        )
      )
    }
  // format: ON

}
