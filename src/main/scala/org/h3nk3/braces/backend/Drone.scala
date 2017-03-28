package org.h3nk3.braces.backend

import akka.actor.{ActorLogging, Props}
import akka.persistence.PersistentActor
import org.h3nk3.braces.backend.DroneManager.SurveillanceArea
import org.h3nk3.braces.domain.Domain._

object DroneActor {
  def props(droneId: Int, surveillanceArea: SurveillanceArea): Props = Props(new DroneActor(droneId, surveillanceArea))

  sealed trait DroneEvent extends Serializable
  final case class DroneDataEvent(id: Int, status: DroneStatus, lat: Double, long: Double, velocity: Double, direction: Int, batteryPower: Int) extends DroneEvent
}

class DroneActor(droneId: Int, surveillanceArea: SurveillanceArea) extends PersistentActor with ActorLogging {
  import org.h3nk3.braces.backend.DroneActor._

  log.info(s"Drone: $droneId started.")

  override def persistenceId: String = "Drone-" + self.path.name

  var droneData: DroneData = null

  override def receiveCommand: Receive = {
    case DroneData(id, status, info) =>
      persist(DroneDataEvent(id, status, info.position.lat, info.position.long, info.velocity, info.direction, info.batteryPower))(updateState)
  }

  override def receiveRecover: Receive = {
    case dde: DroneDataEvent => updateState(dde)
  }

  val updateState: DroneDataEvent => Unit = {
    case dde: DroneDataEvent =>
      droneData = DroneData(dde.id, dde.status, DroneInfo(DronePosition(dde.lat, dde.long), dde.velocity, dde.direction, dde.batteryPower))
  }

}

final case class ServerCommand() // TODO do we need those?
