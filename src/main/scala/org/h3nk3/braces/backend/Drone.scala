package org.h3nk3.braces.backend

import akka.actor.{ActorLogging, Props}
import akka.cluster.singleton.{ClusterSingletonProxy, ClusterSingletonProxySettings}
import akka.persistence.PersistentActor
import org.h3nk3.braces.backend.DroneManager.SurveillanceArea
import org.h3nk3.braces.domain.Domain._

object DroneActor {
  def props(): Props = Props[DroneActor]

  case class DroneInfo(id: Int, status: DroneStatus, knownUptime: Long, position: DronePosition, distanceCovered: Double)
  case class DroneInitData(id: Int, surveillanceArea: SurveillanceArea)
  case object InitDrone

  sealed trait DroneCommand
  final case object DroneInfoCommand extends DroneCommand

  sealed trait DroneEvent extends Serializable
  final case class DroneDataEvent(id: Int, status: DroneStatus, lat: Double, long: Double, velocity: Double, direction: Int, batteryPower: Int, distanceCovered: Double, createdTime: Long = System.currentTimeMillis()) extends DroneEvent
}

class DroneActor extends PersistentActor with ActorLogging {
  import org.h3nk3.braces.backend.DroneActor._

  override def postStop(): Unit = {
    context.system.actorOf(ClusterSingletonProxy.props("/user/droneManager", ClusterSingletonProxySettings(context.system))) ! DroneManager.DroneStopped(self)
  }

  override def persistenceId: String = "Drone-" + self.path.name

  var droneId: Int = 0
  var surveillanceArea: SurveillanceArea = null
  var droneInfo: DroneInfo = null

  override def receiveCommand: Receive = {
    case InitDrone =>
      context.system.actorOf(ClusterSingletonProxy.props("/user/droneManager", ClusterSingletonProxySettings(context.system))) ! DroneManager.DroneStarted(self)
    case DroneInitData(id, area) =>
      log.info(s">> Drone: $droneId initialized with ${self.path} <<")
      // FIXME: send instructions to drone to initiate work
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
