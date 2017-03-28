package org.h3nk3.braces.web

import akka.stream.scaladsl.Source
import org.h3nk3.braces.AkkaSpec
import org.h3nk3.braces.domain.Domain.{DroneInfo, DronePosition}

class DroneInfoIngestionServiceSpec extends AkkaSpec 
with DroneInfoIngestionService {
  
  "DroneInfoIngestionService" should {
    "fail if consuming side has not started" in {
      val incoming = Source.single(DroneInfo(DronePosition(1,1), 1, 0, 35))
      ingestionHub.runWith(incoming)
    }
  }

}
