package org.h3nk3.braces.backend

import akka.actor.Props
import akka.persistence.PersistentActor
import org.h3nk3.braces.backend.DroneManager.SurveillanceArea

object PersistentDrone {
  def props(droneId: String, surveillanceArea: SurveillanceArea): Props = 
    Props(new PersistentDrone(droneId, surveillanceArea))
}

class PersistentDrone(droneId: String, surveillanceArea: SurveillanceArea) extends PersistentActor {
  override def receiveRecover: Receive = ???
  override def receiveCommand: Receive = ???
  override def persistenceId: String = ???
}
