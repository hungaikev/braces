package org.h3nk3.braces.domain

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.HttpEntity
import org.h3nk3.braces.AkkaSpec
import org.scalatest.concurrent.ScalaFutures

class DomainMarshallingSpec extends AkkaSpec with ScalaFutures {

  import org.h3nk3.braces.domain.Domain._
  
  "Domain" should {
    "be marshalled to json" in {
      val marshalled = Marshal(DronePosition(12, 42)).to[HttpEntity]
      marshalled.futureValue should === ("")
    }
  }

}
