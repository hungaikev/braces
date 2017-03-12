package org.h3nk3.braces.web

import akka.http.scaladsl.server.{Directives, HttpApp}
import akka.stream.ActorMaterializer

object HttpMain extends HttpApp with App 
  with Directives 
  with OurOwnWebSocketSupport { 

  startServer("127.0.0.1", 8000)

  override lazy val materializer = ActorMaterializer()(systemReference.get
  
  // format: OFF
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
  // format: ON

}
