package org.h3nk3.braces.drone

import java.util.concurrent.ThreadLocalRandom

import akka.NotUsed
import akka.actor.{ActorSystem, Cancellable}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Source}
import akka.util.ByteString
import org.h3nk3.braces.domain.Domain._
import org.h3nk3.braces.domain.OurDomainJsonSupport

import scala.concurrent.duration._

object DroneApp_Step2_CsvOverTcp extends App with OurDomainJsonSupport {

  val droneId = ThreadLocalRandom.current().nextInt(10)

  implicit val sys = ActorSystem("DroneSystem-real-" + droneId)
  implicit val mat = ActorMaterializer()
  import sys.dispatcher

  val myDroneId = "9S"

  private val req = HttpRequest(
    HttpMethods.POST, s"http://127.0.0.1:8080/drone/tcp/$myDroneId",
    entity = HttpEntity(ContentTypes.`text/csv(UTF-8)`, emitPositionAndMetrics)
  )
  
  Http().singleRequest(req)
    
//    // TODO show how to reconnect
//    .onComplete { _ =>
//      Http().singleRequest(req)
//    }



  def emitPositionAndMetrics: Source[ByteString, Cancellable] =
      Source.tick(initialDelay = 1.second, interval = 1.second, tick = NotUsed)
        .map(_ => getCurrentPosition)
        .via(renderAsJson)


  def getCurrentPosition: DronePosition =
    DronePosition(13, 37) // TODO make it move
  
  val renderAsJson: Flow[DronePosition, ByteString, NotUsed] =
    Flow[DronePosition]
      .mapAsync(parallelism = 1)(p => Marshal(p).to[HttpEntity].flatMap(_.toStrict(1.seconds)))
      .map(_.data)
  
}
