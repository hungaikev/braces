package org.h3nk3.braces.web

import akka.stream.scaladsl.Source
import org.h3nk3.braces.AkkaSpec
import org.h3nk3.braces.domain.Domain._

class DroneInfoIngestionServiceSpec extends AkkaSpec 
with DroneInfoIngestionService {
  
  "DroneInfoIngestionService" should {
    "fail if consuming side has not started" in {
      val incoming = Source.single(DroneData(1, Ready, DronePosition(1,1), 1, 0, 35))
      ingestionHub.runWith(incoming)
    }
  }

}
