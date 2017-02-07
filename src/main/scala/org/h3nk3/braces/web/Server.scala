package org.h3nk3.braces.web

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.io.StdIn
import akka.stream.scaladsl.Flow

object Server {
  def main(args: Array[String]) = {
    implicit val system = ActorSystem("braces-frontend")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val wsFlow: Flow[Message, Message, Any] = ???

    val route =
      path("ping") {
        get {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>Pinged at ${System.currentTimeMillis}</h1>"))
        } ~
          path("ws") {
            println("Incoming WS connection accepted")
            handleWebSocketMessages(wsFlow)
          }
      }

      val httpBindingFuture = Http().bindAndHandle(route, "localhost", 8080)

      println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
      StdIn.readLine() // let it run until user presses return
      httpBindingFuture
        .flatMap(_.unbind()) // trigger unbinding from the port
        .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
