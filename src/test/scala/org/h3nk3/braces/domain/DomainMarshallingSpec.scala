package org.h3nk3.braces.domain

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import org.h3nk3.braces.AkkaSpec
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.Future

class DomainMarshallingSpec extends AkkaSpec with ScalaFutures {

  import org.h3nk3.braces.domain.Domain._
  
  "Domain" should {
    "be marshalled to json" in {
      val marshalled: Future[HttpEntity] = Marshal(DronePosition(12, 42)).to[HttpEntity]
      val entity: HttpEntity = marshalled.futureValue
      entity should === (HttpEntity(ContentTypes.`application/json`, "{\"lat\":12.0,\"long\":42.0}"))
    }
  }

}
