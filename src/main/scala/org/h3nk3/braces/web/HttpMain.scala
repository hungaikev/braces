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
import akka.http.scaladsl.model.ws.TextMessage.{Streamed, Strict}
import akka.http.scaladsl.server.{Directives, HttpApp}

import scala.io.StdIn
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.util.Random

object HttpMain extends HttpApp with App 
  with Directives 
  with OurOwnWebSocketSupport { 

  startServer("127.0.0.1", 8000)

  override def route =
    // step 0 --- just a hello world 
    get {
      complete("Hello world!")
    } ~
    // step 1 --- websocket
    pathSingleSlash {
      getFromResource("braces.html")
    } ~
    path("ws") {
      handleWebSocketMessages(websocketEcho)
    }
  
  
}
