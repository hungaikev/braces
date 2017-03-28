package org.h3nk3.braces.domain

import java.util.Date

import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.AutoDerivation

/**
 * Always use as: `import org.h3nk3.braces.domain.Domain._`
 * in order to enable automatic marshalling via Circe.
 */
trait Domain extends FailFastCirceSupport with AutoDerivation {

  case class Base(id: String, position: DronePosition)

  // Events
  trait BracesEvent

  case class InitializeClient(clientId: String) extends BracesEvent

  case class DroneData(id: Int, status: DroneStatus, info: DroneInfo) // better names?

  trait DroneStatus extends Serializable
  case object Charging    extends DroneStatus
  case object Ready       extends DroneStatus
  case object Operating   extends DroneStatus
  case object Maintenance extends DroneStatus
  case object Stopped     extends DroneStatus

  /** server can signal commands to drone? */
  final case class DroneCommand()

  final case class DroneInfo(position: DronePosition, velocity: Double, direction: Int, batteryPower: Int)

  final case class DronePosition(lat: Double, long: Double)

  final case class ServerCommand() // TODO do we need those?

  final case class Image(droneId: Int, imageId: Long, date: Date, position: DronePosition, pieceResolution: Int, pieces: Array[Array[Int]])
}

object Domain extends Domain
