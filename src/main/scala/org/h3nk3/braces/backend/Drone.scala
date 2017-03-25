package org.h3nk3.braces.backend

import akka.actor.{ActorLogging, Props}
import akka.persistence.PersistentActor
import org.h3nk3.braces.backend.DroneManager.SurveillanceArea
import org.h3nk3.braces.domain.Domain.DronePosition

trait DroneStatus
case object Charging extends DroneStatus
case object Ready extends DroneStatus
case object Operating extends DroneStatus
case object Maintenance extends DroneStatus
case object Stopped extends DroneStatus

object DroneActor {
  def props(droneId: Int, surveillanceArea: SurveillanceArea): Props = Props(new DroneActor(droneId, surveillanceArea))

  final case class Drone(id: Int, status: DroneStatus, info: DroneInfo)
  final case class DroneInfo(id: Int, position: DronePosition, velocity: Double, direction: Int, batteryPower: Int)
  /** server can signal commands to drone? */
  final case class DroneCommand(id: String)
}

class DroneActor(droneId: Int, surveillanceArea: SurveillanceArea) extends PersistentActor with ActorLogging {
  log.info(s"Drone: $droneId started.")

  override def persistenceId: String = "Drone-" + self.path.name

  override def receiveRecover: Receive = ???

  override def receiveCommand: Receive = ???

}


final case class ServerCommand() // TODO do we need those?
