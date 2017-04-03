package org.h3nk3.braces.web

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import org.h3nk3.braces.domain.{CsvDomain, DroneData}

import scala.concurrent.Promise

object HttpMain_Step1_CsvMetrics extends App 
  with Directives with OurOwnWebSocketSupport 
  with CsvDomain {
  
  implicit val system = ActorSystem("BracesBackend")
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher = system.dispatcher
  
  
  // format: OFF
  def routes =
    path("drone" / "data") {
      entity(asSourceOf[DroneData]) { infos =>
        infos.to(???).run()
        neverRespond()
      }
    }
  // format: ON
  
  
  Http().bindAndHandle(routes, "127.0.0.1", 8080)
  
  
  private def neverRespond() = 
    complete(Promise[String]().future)
}
