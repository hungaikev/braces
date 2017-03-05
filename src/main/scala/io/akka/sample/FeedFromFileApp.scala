package io.akka.sample

import java.util.concurrent.ThreadLocalRandom

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.scaladsl.{FileIO, Framing, Sink, Source}
import akka.stream.{ActorAttributes, ActorMaterializer, Supervision}
import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import io.akka.sample.ml.IoTDeepLearningDetection
import io.akka.sample.model.DataSourcePaths

import scala.util.Try

object FeedFromFileApp extends AkkaApp
  with IoTDeepLearningDetection {

  private val incomingBytes: Source[ByteString, NotUsed] = Source.cycle(() =>
    FileIO.fromPath(DataSourcePaths.State_Good) ::
      FileIO.fromPath(DataSourcePaths.State_Good) ::
      FileIO.fromPath(DataSourcePaths.State_Good) ::
      Nil toIterator
  ).flatMapConcat(identity)
  
  val measurementLines =
  // TODO show cycles and flatmapping then
  incomingBytes
//  // repeat this entire thing periodically
//    FileIO.fromPath(DataSourcePaths.State_Good)
//      .concat(FileIO.fromPath(DataSourcePaths.State_Bad_Loop_1))
//      .concat(FileIO.fromPath(DataSourcePaths.State_Good))
//      .concat(FileIO.fromPath(DataSourcePaths.State_Good))
//      .concat(FileIO.fromPath(DataSourcePaths.State_Good))
//      .concat(FileIO.fromPath(DataSourcePaths.State_Bad_Loop_1))
//      // repeat this entire thing periodically
      .via(Framing.delimiter(ByteString("\n"), maximumFrameLength = 1000))
      .map(_.utf8String)

  val dataPoints = measurementLines
    .via(CsvSupport.takeColumns(Set("LinAccX (g)", "LinAccY (g)", "LinAccZ (g)"))) 
    .mapConcat(_.flatMap(col => Try(col.toDouble).toOption))

  dataPoints
    .via(this.scoringFlow)
    .runWith(Sink.ignore) // TODO show println instead, or Sink.actorRefWithAck


  Console.readLine()
  system.terminate()
  
  /*
    bytesToMeasurements(incomingBytes)
  
    private def bytesToMeasurements(bytes: Source[ByteString, NotUsed]) = {
      val measurementLines =
        bytes
          .via(Framing.delimiter(ByteString("\n"), maximumFrameLength = 1000))
          .map(_.utf8String)
  
      measurementLines
        .via(CsvSupport.takeColumns(Set("LinAccX (g)", "LinAccY (g)", "LinAccZ (g)"))) // TODO show: .async
        .mapConcat(_.flatMap(col => Try(col.toDouble).toOption))
    }
   */
  
  
}
