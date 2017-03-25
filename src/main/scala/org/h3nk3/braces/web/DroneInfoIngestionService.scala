package org.h3nk3.braces.web

import java.util.concurrent.atomic.AtomicReference

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.{Keep, MergeHub, Sink, Source}
import org.h3nk3.braces.domain.Domain._

trait DroneInfoIngestionService {

  implicit def materializer: Materializer

  val ingestionHub: Sink[DroneInfo, NotUsed] =
    DroneInfoIngestionService.hubIngestion.get() match {
      case null => throw new Exception("Not initialized consuming side of hub yet!")
      case sink => sink
    }

  def initIngestionHub[M](sink: Sink[DroneInfo, M]): M =
    DroneInfoIngestionService.hubIngestion.get() match {
      case null =>
        val (mergeSink, mat) = 
          MergeHub.source(perProducerBufferSize = 32)
          .toMat(sink)(Keep.both)
            .run()
        
        if (DroneInfoIngestionService.hubIngestion.compareAndSet(null, mergeSink))
          mat
        else {
          mergeSink.runWith(Source.empty) // complete the hub // TODO does it really?
          ??? // FIXME
        }
        
    }
}

object DroneInfoIngestionService {
  private[braces] val hubIngestion = new AtomicReference[Sink[DroneInfo, NotUsed]]()
  private[braces] val hubSource = new AtomicReference[Source[DroneInfo, NotUsed]]()
}
