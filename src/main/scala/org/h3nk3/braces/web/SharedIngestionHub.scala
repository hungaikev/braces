package org.h3nk3.braces.web

import java.util.concurrent.atomic.AtomicReference

import akka.NotUsed
import akka.stream.{KillSwitches, Materializer}
import akka.stream.scaladsl.{Keep, MergeHub, Sink, Source}
import org.h3nk3.braces._
import org.h3nk3.braces.domain.DroneData

trait SharedIngestionHub {

  implicit def materializer: Materializer

  lazy val ingestionHub: Sink[DroneData, NotUsed] =
    SharedIngestionHub.hubIngestion.get() match {
      case null => throw new Exception("Not initialized consuming side of hub yet!")
      case sink => sink
    }

  private val killSwitch = KillSwitches.shared("ingestion-hub")

  def initIngestionHub[M](sink: Sink[DroneData, M]): M =
    SharedIngestionHub.hubIngestion.get() match {
      case null =>
        val (mergeSink, mat) = 
          MergeHub.source[DroneData](perProducerBufferSize = 32)
            .via(killSwitch.flow)
            .toMat(sink)(Keep.both)
            .run()
        
        if (SharedIngestionHub.hubIngestion.compareAndSet(null, mergeSink))
          mat
        else {
          mergeSink.runWith(Source.empty) // complete the hub
          null.asInstanceOf[M]
        }
        
    }
  
  def shutdownIngestionHub(): Unit =
    killSwitch.shutdown()
}

object SharedIngestionHub {
  private[braces] val hubIngestion = new AtomicReference[Sink[DroneData, NotUsed]]()
  private[braces] val hubSource = new AtomicReference[Source[DroneData, NotUsed]]()
}
