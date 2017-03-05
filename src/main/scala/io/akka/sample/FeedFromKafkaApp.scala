package io.akka.sample

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.directives.FramedEntityStreamingDirectives
import akka.http.scaladsl.server.{Directives, HttpApp, Route}
import akka.http.scaladsl.settings.ServerSettings
import akka.kafka.scaladsl.Consumer
import akka.kafka.scaladsl.Consumer.Control
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.{Flow, Framing, Sink, Source}
import akka.stream.{ActorAttributes, ActorMaterializer}
import akka.util.ByteString
import io.akka.sample.ml.IoTDeepLearningDetection
import io.akka.sample.model.MachineStatus
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}
import spray.json._

import scala.util.Try

object FeedFromKafkaApp extends App with HttpApp with Directives
  with FramedEntityStreamingDirectives with MyJsonFormats
  with IoTDeepLearningDetection {

  implicit val system = ActorSystem(Logging.simpleName(getClass).replace("$", ""))
  implicit val dispatcher = system.dispatcher
  implicit val config = system.settings.config
  implicit val materializer = ActorMaterializer()

  
  val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
    .withBootstrapServers("localhost:9092")
    .withGroupId("group1")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  val partition = 0
  val subscription = Subscriptions.topics("measurements")

  
  implicit val jsonStreaming = EntityStreamingSupport.json()
  
  
  startServer("127.0.0.1", 8080, ServerSettings(system), system)

  override def route: Route =
    get {
      val scores: Source[MachineStatus, Control] =
        Consumer.plainSource(consumerSettings, subscription)
          .map { record => ByteString(record.value()) }

          .via(bytesToMeasurements)
          .via(this.scoringFlow) // again, the same code!
              
      complete(scores)
    } 
  

  private lazy val bytesToMeasurements: Flow[ByteString, Double, NotUsed] = {
    val measurementLines =
      Flow[ByteString]
        .via(Framing.delimiter(ByteString("\n"), maximumFrameLength = 1000))
        .map(_.utf8String)

    measurementLines
      .via(CsvSupport.takeColumns(Set("LinAccX (g)", "LinAccY (g)", "LinAccZ (g)")))/**/
      .mapConcat(_.flatMap(col => Try(col.toDouble).toOption))
      .mapMaterializedValue(_ => NotUsed)
  }
}

trait MyJsonFormats extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val MachineStatusFormat: RootJsonFormat[MachineStatus] = ???
}
