package org.h3nk3.braces.web

import akka.stream.scaladsl.{Sink, Source}
import org.h3nk3.braces.AkkaSpec
import org.h3nk3.braces.domain.Domain._

class DroneInfoIngestionServiceSpec extends AkkaSpec 
  with DroneInfoIngestionService {
  
  "DroneInfoIngestionService" should {
    "fail if consuming side has not started" in {
      val incoming = Source.single(DroneData(1, Ready, DronePosition(1,1), 1, 0, 35))
      val ex = intercept[Exception] {
        ingestionHub.runWith(incoming)
      }
      info("Expected exception: " + ex.getMessage)
    }
    
    "attach to existing hub" in {
      initIngestionHub(Sink.actorRef(testActor, onCompleteMessage = "FULL_SHUTDOWN"))
      
      // new incoming connection from drone:
      val i1 = DroneInfo("2B", DronePosition(10.0, 121.0), 1.0, 1, 92)
      ingestionHub.runWith(Source.single(i1))
      
      // another drone connects
      val i2 = DroneInfo("A2", DronePosition(10.0, 121.0), 1.0, 1, 92)
      ingestionHub.runWith(Source.single(i1))
      
      // ^^^^--- these operations run async!
      
      expectMsgAnyOf(i1, i2)
      expectMsgAnyOf(i1, i2)
    }
  }

}
