package org.h3nk3.braces.domain

import akka.actor.Props
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.persistence.PersistentActor
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.AutoDerivation
import org.h3nk3.braces.backend.DroneManager.SurveillanceArea

/**
 * Always use as: `import org.h3nk3.braces.domain.Domain._`
 * in order to enable automatic marshalling via Circe.
 */
trait Domain extends FailFastCirceSupport with AutoDerivation {

  case class Base(id: String, position: DronePosition)

  // Events
  trait BracesEvent

  case class InitializeClient(clientId: String) extends BracesEvent

  case class DroneData(id: String, status: DroneStatus, info: DroneInfo) // better names?

  trait DroneStatus
  case object Charging    extends DroneStatus
  case object Ready       extends DroneStatus
  case object Operating   extends DroneStatus
  case object Maintenance extends DroneStatus
  case object Stopped     extends DroneStatus

  /** server can signal commands to drone? */
  final case class DroneCommand()

  final case class DroneInfo(id: String, position: DronePosition, velocity: Double, direction: Int, batteryPower: Int)

  final case class DronePosition(lat: Double, long: Double)

  final case class ServerCommand() // TODO do we need those?

}

object Domain extends Domain
