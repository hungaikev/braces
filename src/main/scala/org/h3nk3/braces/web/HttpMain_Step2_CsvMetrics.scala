package org.h3nk3.braces.web

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshalling
import akka.http.scaladsl.server.{Directives, StandardRoute}
import akka.http.scaladsl.unmarshalling._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import akka.util.ByteString

import scala.concurrent.{Future, Promise}

object HttpMain_Step2_CsvMetrics extends App 
  with Directives with OurOwnWebSocketSupport 
  with DroneInfoIngestion { 

  import org.h3nk3.braces.domain.Domain._
  
  implicit val system = ActorSystem("HttpApp")
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher = system.dispatcher
  
  
  
  implicit val csvStreaming = EntityStreamingSupport.csv()
  implicit val csvUnmarshalling: Unmarshaller[ByteString, DroneData] = 
    Unmarshaller.strict { bs => 
      bs.utf8String.split(",").toVector
    }
  implicit val csvMarshalling: Unmarshaller[ByteString, DroneData] = 
    Unmarshaller.strict { bs => ??? }
  
  // format: OFF
  def routes =
    path("drone" / "data") {
      entity(asSourceOf[DroneData]) { infos =>
        infos.to(ingestionHub).run()
        neverRespond()
      }
    }
  // format: ON
  
  
  Http().bindAndHandle(routes, "127.0.0.1", 8080)
  
  
  private def neverRespond() = 
    complete(Promise[String]().future)
}