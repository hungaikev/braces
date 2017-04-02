package org.h3nk3.braces.domain

import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.util.ByteString
import org.h3nk3.braces.domain.Domain.DroneData

trait CsvDomain {
  implicit val csvStreaming = EntityStreamingSupport.csv()

  implicit val csvUnmarshalling: Unmarshaller[ByteString, DroneData] =
    Unmarshaller.strict { bs =>
      val it = bs.utf8String.split(",").iterator
      import it.next
      DroneData(
        next().toInt,
        Domain.DroneStatus.fromString(next()),
        Domain.Position(next().toDouble, next().toDouble),
        next().toDouble,
        next().toInt,
        next().toInt
      )
    }

}

object CsvDomain extends CsvDomain {

} 
