package io.akka.sample

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.server.{Directives, HttpApp, Route}
import akka.http.scaladsl.settings.ServerSettings
import akka.stream.scaladsl.{Framing, Sink, Source}
import akka.stream.{ActorAttributes, ActorMaterializer}
import akka.util.ByteString
import io.akka.sample.ml.IoTDeepLearningDetection
import io.akka.sample.model.MachineStatus

import scala.util.Try

object FeedFromHttpPostApp extends App with HttpApp with Directives
  with IoTDeepLearningDetection {

  implicit val system = ActorSystem(Logging.simpleName(getClass).replace("$", ""))
  implicit val dispatcher = system.dispatcher
  implicit val config = system.settings.config
  implicit val materializer = ActorMaterializer()

  startServer("127.0.0.1", 8080, ServerSettings(system), system)

  override def route: Route =
    post {
      extractRequestEntity { entity =>
        complete {
          val datapoints = bytesToMeasurements(entity.withoutSizeLimit().dataBytes)

          datapoints
            .via(this.scoringFlow)

            .log("score")
            .withAttributes(ActorAttributes.logLevels(onElement = Logging.InfoLevel)) // TODO: explain attributes

            .runWith(Sink.fold(0) {
              case (failuresCount, MachineStatus.Ok) => failuresCount // no more failures 
              case (failuresCount, MachineStatus.Misbehaving) => failuresCount + 1 // one more failure prediction!
            })

            .map { after => s"$after predictions were failures!" }

        }
      }
    }

  private def bytesToMeasurements(bytes: Source[ByteString, Any]): Source[Double, NotUsed] = {
    val measurementLines =
      bytes
        .via(Framing.delimiter(ByteString("\n"), maximumFrameLength = 1000))
        .map(_.utf8String)

    measurementLines
      .via(CsvSupport.takeColumns(Set("LinAccX (g)", "LinAccY (g)", "LinAccZ (g)"))) // TODO show: .async
      .mapConcat(_.flatMap(col => Try(col.toDouble).toOption))
      .mapMaterializedValue(_ => NotUsed)
  }
}
