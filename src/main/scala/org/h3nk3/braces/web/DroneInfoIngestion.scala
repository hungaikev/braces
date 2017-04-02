package org.h3nk3.braces.web

import java.util.concurrent.atomic.AtomicReference

import akka.NotUsed
import akka.stream.{KillSwitches, Materializer}
import akka.stream.scaladsl.{Keep, MergeHub, Sink, Source}
import org.h3nk3.braces.domain.Domain._

trait DroneInfoIngestion {

  implicit def materializer: Materializer

  lazy val ingestionHub: Sink[DroneData, NotUsed] =
    DroneInfoIngestion.hubIngestion.get() match {
      case null => throw new Exception("Not initialized consuming side of hub yet!")
      case sink => sink
    }
  

  def initIngestionHub[M](sink: Sink[DroneData, M]): M =
    DroneInfoIngestion.hubIngestion.get() match {
      case null =>
        val (mergeSink, mat) = 
          MergeHub.source(perProducerBufferSize = 32)
          .toMat(sink)(Keep.both)
            .run()
        
        if (DroneInfoIngestion.hubIngestion.compareAndSet(null, mergeSink))
          mat
        else {
          mergeSink.runWith(Source.empty) // complete the hub
          null.asInstanceOf[M]
        }
        
    }
  
  def shutdownIngestionHub(): Unit =
    ???
}

object DroneInfoIngestion {
  private[braces] val hubIngestion = new AtomicReference[Sink[DroneData, NotUsed]]()
  private[braces] val hubSource = new AtomicReference[Source[DroneData, NotUsed]]()
}
