package io.akka.sample

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{BinaryMessage, WebSocketRequest}
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FileIO, Flow, Sink, Source}
import io.akka.sample.ml.IoTDeepLearningDetection
import io.akka.sample.model.DataSourcePaths

object WebSocketClientApp extends App with Directives
  with IoTDeepLearningDetection {

  implicit val system = ActorSystem(Logging.simpleName(getClass).replace("$", ""))
  implicit val dispatcher = system.dispatcher
  implicit val config = system.settings.config
  implicit val materializer = ActorMaterializer()
  
  private val sensorBytes = Source.cycle(() =>
    FileIO.fromPath(DataSourcePaths.State_Good) ::
      FileIO.fromPath(DataSourcePaths.State_Good) ::
      FileIO.fromPath(DataSourcePaths.State_Good) ::
      Nil toIterator
  ).flatMapConcat(identity)
  
  
  Http().singleWebSocketRequest(WebSocketRequest("ws://127.0.0.1:8080/"),
    Flow.fromSinkAndSource(
      Sink.ignore,
      sensorBytes.map( bs => BinaryMessage(bs))
    ))  
  
}
