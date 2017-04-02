package org.h3nk3.braces.web

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import org.h3nk3.braces.domain.Domain

object HttpMain_Step0 extends App 
  with Directives with OurOwnWebSocketSupport { 

  implicit val system = ActorSystem("HttpApp")
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher = system.dispatcher

  def routes =
    get {
      complete("Hello world!")
    }

  Http().bindAndHandle(routes, "127.0.0.1", 8080)





}
