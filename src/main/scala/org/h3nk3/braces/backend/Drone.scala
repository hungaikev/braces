package org.h3nk3.braces.backend

import akka.actor.{ActorLogging, ActorRef, Props}
import akka.cluster.singleton.{ClusterSingletonProxy, ClusterSingletonProxySettings}
import akka.persistence.PersistentActor
import org.h3nk3.braces.backend.DroneManager.SurveillanceArea
import org.h3nk3.braces.domain.Domain._

object DroneActor {
  def props(): Props = Props[DroneActor]

  final val DroneName = "Drone"

  case class DroneInitData(id: Int, surveillanceArea: SurveillanceArea) extends Serializable
  case object InitDrone extends Serializable

  sealed trait DroneCommand extends Serializable
  final case object DroneInfoCommand extends DroneCommand

  sealed trait DroneEvent extends Serializable
  final case class DroneDataEvent(id: Int, status: DroneStatus, lat: Double, long: Double, velocity: Double, direction: Int, batteryPower: Int, distanceCovered: Double, createdTime: Long = System.currentTimeMillis()) extends DroneEvent
}

class DroneActor extends PersistentActor with ActorLogging {
  import org.h3nk3.braces.backend.DroneActor._

  override def postStop(): Unit = {
    context.system.actorOf(ClusterSingletonProxy.props("/user/droneManager", ClusterSingletonProxySettings(context.system))) ! DroneManager.DroneStopped(self)
  }

  override def persistenceId: String = "Drone" + "-" + self.path.name

  var position: DronePosition = DronePosition(0, 0)
  var droneId: Int = 0
  var surveillanceArea: SurveillanceArea = _


  val droneManager: ActorRef = context.system.actorOf(DroneManager.props)
  override def preStart(): Unit = 
   droneManager ! DroneManager.DroneStarted(self)
  
  
  override def receiveCommand: Receive = {
    case DroneInitData(id, area) =>
      log.info(s">> Drone: $droneId initialized with ${self.path} <<")
      // FIXME: send instructions to drone to initiate work

    case DroneData(id, status, pos, velocity, direction, batteryPower) =>
      val distance = calcDistance(pos)
      val event = DroneDataEvent(id, status, pos.lat, pos.long, velocity, direction, batteryPower, distance)
      persist(event)(updateState)
  }

  override def receiveRecover: Receive = {
    case dde: DroneDataEvent => updateState(dde)
  }

  val updateState: DroneDataEvent => Unit = {
    case dde: DroneDataEvent =>
      val knownUptime = System.currentTimeMillis() - dde.createdTime
      log.info("Drone data event: " + (dde.id, dde.status, knownUptime, DronePosition(dde.lat, dde.long), dde.distanceCovered))
  }

  private def calcDistance(pos: DronePosition): Double = {
    Math.sqrt(Math.pow(Math.abs(pos.lat - position.lat), 2) + Math.pow(Math.abs(pos.long - position.long), 2))
  }
}
