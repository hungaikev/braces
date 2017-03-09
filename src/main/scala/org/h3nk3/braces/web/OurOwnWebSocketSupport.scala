package org.h3nk3.braces.web

import akka.NotUsed
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.model.ws.TextMessage.{Streamed, Strict}
import akka.stream.scaladsl.Flow

import scala.util.Random

trait OurOwnWebSocketSupport {

  val websocketEcho: Flow[Message, Message, Any] =
    Flow[Message]
    .via(toStrictText)
      .map { text =>
          Thread.sleep(Random.nextInt(100))
          text
      }
  
  
  def toStrictText: Flow[Message, TextMessage.Strict, NotUsed] = {
    Flow[Message]
      .map(_.asTextMessage)
      .map {
        case t: Streamed => TextMessage(t.getStrictText)
        case t: Strict => t
      }
  }

}
