package org.h3nk3.braces.domain

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.{Marshaller, Marshalling}
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.server.directives.FramedEntityStreamingDirectives
import spray.json.{DefaultJsonProtocol, RootJsonWriter}

import scala.concurrent.Future

trait OurDomainJsonSupport extends SprayJsonSupport with DefaultJsonProtocol 
with FramedEntityStreamingDirectives {
  
  // TODO technically should not be needed to go as low level... easier for me to impl actually heh
  implicit def directlyToString[P](implicit m: RootJsonWriter[P]): Marshaller[P, String] =
    Marshaller { _ => c => 
      Future.successful {
        Marshalling.WithFixedContentType(ContentTypes.`text/plain(UTF-8)`, () => m.write(c).toString()) :: Nil
      }
    }
}
