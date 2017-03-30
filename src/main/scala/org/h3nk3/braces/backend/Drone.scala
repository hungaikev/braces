package org.h3nk3.braces.backend

import akka.actor.{ActorLogging, Props}
import akka.persistence.PersistentActor
import org.h3nk3.braces.backend.DroneManager.SurveillanceArea
import org.h3nk3.braces.domain.Domain._

object DroneActor {
  def props(droneId: Int, surveillanceArea: SurveillanceArea): Props = Props(new DroneActor(droneId, surveillanceArea))

  case class DroneInfo(id: Int, status: DroneStatus, knownUptime: Long, position: DronePosition, distanceCovered: Double)

  sealed trait DroneCommand
  final case object DroneInfoCommand extends DroneCommand

  sealed trait DroneEvent extends Serializable
  final case class DroneDataEvent(id: Int, status: DroneStatus, lat: Double, long: Double, velocity: Double, direction: Int, batteryPower: Int, distanceCovered: Double, createdTime: Long = System.currentTimeMillis()) extends DroneEvent
}

class DroneActor(droneId: Int, surveillanceArea: SurveillanceArea) extends PersistentActor with ActorLogging {
  import org.h3nk3.braces.backend.DroneActor._

  log.info(s"Drone: $droneId started.")

  override def persistenceId: String = "Drone-" + self.path.name

  var droneInfo: DroneInfo = null

  override def receiveCommand: Receive = {
    case DroneData(id, status, position, velocity, direction, batteryPower) =>
      val distance = calcDistance(position)
      persist(DroneDataEvent(id, status, position.lat, position.long, velocity, direction, batteryPower, distance))(updateState)
    case DroneInfoCommand =>
      sender ! droneInfo
  }

  override def receiveRecover: Receive = {
    case dde: DroneDataEvent => updateState(dde)
  }

  val updateState: DroneDataEvent => Unit = {
    case dde: DroneDataEvent =>
      val knownUptime = System.currentTimeMillis() - dde.createdTime
      droneInfo = DroneInfo(dde.id, dde.status, knownUptime, DronePosition(dde.lat, dde.long), dde.distanceCovered)
  }

  private def calcDistance(pos: DronePosition): Double = {
    if (droneInfo eq null) 0.0
    else Math.sqrt(Math.pow(Math.abs(pos.lat - droneInfo.position.lat), 2) + Math.pow(Math.abs(pos.long - droneInfo.position.long), 2))
  }
}
