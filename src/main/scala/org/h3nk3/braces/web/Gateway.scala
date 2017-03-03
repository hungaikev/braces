package org.h3nk3.braces.web

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import spray.json._
import DefaultJsonProtocol._
import akka.NotUsed

import scala.io.StdIn
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.util.Random


object Gateway {
  def main(args: Array[String]) = {
    implicit val system = ActorSystem("braces-frontend")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val wsFlow: Flow[Message, Message, Any] =
      Flow[Message].mapConcat {
        case tm: TextMessage =>
          Thread.sleep(Random.nextInt(100))
          TextMessage(Source.single(tm.getStrictText)) :: Nil
        case _ => Nil
      }

    val route =
      path("ws") {
        handleWebSocketMessages(wsFlow)
      }

    val httpBindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println(s"Gateway online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    httpBindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }

  // Domain objects
  case class Position(lat: Double, long: Double)

  case class Drone(id: String, position: Position)

  case class Base(id: String, position: Position)

  // Events
  trait BracesEvent

  case class InitializeClient(clientId: String) extends BracesEvent

}
