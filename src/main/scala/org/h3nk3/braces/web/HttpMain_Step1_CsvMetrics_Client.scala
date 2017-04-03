package org.h3nk3.braces.web

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest}
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import org.h3nk3.braces.domain._

import scala.concurrent.duration._
import scala.concurrent.{Await, Promise}

object HttpMain_Step1_CsvMetrics_Client extends App
  with Directives with OurOwnWebSocketSupport
  with CsvDomain {

  implicit val system = ActorSystem("BracesBackend")
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher = system.dispatcher

  val example =
    DroneData(
      id = 1337,
      status = Operating,
      position = Position(2.0, 5.0),
      velocity = 1.0,
      direction = 90,
      batteryPower = 30
    )


  // client example:
  Http().singleRequest(
    HttpRequest(HttpMethods.POST, uri = "http://127.0.0.1:8080/drone/csv-example")
      .withEntity(HttpEntity(
        ContentTypes.`text/csv(UTF-8)`,
        Source.repeat(NotUsed)
          .mapAsync(1)(_ => Marshal(example).to[ByteString])
      ))
  )

}
