package org.h3nk3.braces.backend

import akka.actor.Props
import akka.persistence.PersistentActor
import org.h3nk3.braces.backend.DroneManager.SurveillanceArea

case class Drone(id: String, status: DroneStatus, info: DroneInfo)

trait DroneStatus
case object Charging extends DroneStatus
case object Ready extends DroneStatus
case object Operating extends DroneStatus
case object Maintenance extends DroneStatus
case object Stopped extends DroneStatus

case class DroneInfo(position: Position, velocity: Double, direction: Int, batteryPower: Int)
case class Position(lat: Double, long: Double)

object DroneActor {
  def props(droneId: String, surveillanceArea: SurveillanceArea): Props = Props(new DroneActor(droneId, surveillanceArea))
}

class DroneActor(droneId: String, surveillanceArea: SurveillanceArea) extends PersistentActor {
  override def receiveRecover: Receive = ???
  override def receiveCommand: Receive = ???
  override def persistenceId: String = ???
}