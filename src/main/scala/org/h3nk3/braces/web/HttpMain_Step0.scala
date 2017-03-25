package org.h3nk3.braces.web

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import org.h3nk3.braces.domain.Domain
import org.h3nk3.braces.domain.Domain._

import scala.concurrent.Future

object HttpMain_Step0 extends App 
  with Directives with OurOwnWebSocketSupport { 

  implicit val system = ActorSystem("HttpApp")
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher = system.dispatcher
  
  Http().bindAndHandle(routes, "127.0.0.1", 8080)

  override def ingestionHub: Sink[Any, Future[Done]] = Sink.ignore

  // format: OFF
  def routes =
    get {
      complete("Hello world!")
    }
  // format: ON
}
