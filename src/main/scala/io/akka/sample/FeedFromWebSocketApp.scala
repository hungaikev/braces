package io.akka.sample

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.{Directives, HttpApp, Route}
import akka.http.scaladsl.settings.ServerSettings
import akka.stream.scaladsl.{Flow, Framing, Sink, Source}
import akka.stream.{ActorAttributes, ActorMaterializer}
import akka.util.ByteString
import io.akka.sample.ml.IoTDeepLearningDetection
import io.akka.sample.model.MachineStatus

import scala.util.Try

object FeedFromWebSocketApp extends App with HttpApp with Directives
  with IoTDeepLearningDetection {

  implicit val system = ActorSystem(Logging.simpleName(getClass).replace("$", ""))
  implicit val dispatcher = system.dispatcher
  implicit val config = system.settings.config
  implicit val materializer = ActorMaterializer()

  startServer("127.0.0.1", 8080, ServerSettings(system), system)

  override def route: Route =
    handleWebSocketMessages {
      Flow[Message]
        .map(_.asBinaryMessage.getStreamedData).flatMapConcat(identity)
        .via(this.bytesToMeasurements)
        .via(this.scoringFlow)

        .log("score")
        .withAttributes(ActorAttributes.logLevels(onElement = Logging.InfoLevel)) // TODO: explain attributes

        .map { after => TextMessage("Done receiving data.") }
    }

  private lazy val bytesToMeasurements: Flow[ByteString, Double, NotUsed] = {
    val measurementLines =
      Flow[ByteString]
        .via(Framing.delimiter(ByteString("\n"), maximumFrameLength = 1000))
        .map(_.utf8String)

    measurementLines
      .via(CsvSupport.takeColumns(Set("LinAccX (g)", "LinAccY (g)", "LinAccZ (g)"))) // TODO show: .async
      .mapConcat(_.flatMap(col => Try(col.toDouble).toOption))
      .mapMaterializedValue(_ => NotUsed)
  }
}
