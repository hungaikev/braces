package org.h3nk3.braces.domain

import java.util.Date

import akka.http.scaladsl.unmarshalling.{FromByteStringUnmarshaller, FromEntityUnmarshaller, Unmarshaller}
import akka.util.ByteString
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.AutoDerivation
import io.circe.{Json, jawn}

/**
 * Always use as: `import org.h3nk3.braces.domain.Domain._`
 * in order to enable automatic marshalling via Circe.
 */
trait Domain {

  case class Base(id: String, position: Position)

  // Events
  trait BracesEvent

  case class InitializeClient(clientId: String) extends BracesEvent

  case class DroneData(id: Int, status: DroneStatus, position: Position, velocity: Double, direction: Int, batteryPower: Int)

  trait DroneStatus extends Serializable
  case object Charging    extends DroneStatus
  case object Ready       extends DroneStatus
  case object Operating   extends DroneStatus
  case object Maintenance extends DroneStatus
  case object Stopped     extends DroneStatus

  /** Commands set to the field-deployed DroneClients */
  trait DroneClientCommand
  final case class SurveilArea(lowerLeft: Position, upperRight: Position) extends DroneClientCommand
  final case object GotoBase extends DroneClientCommand

  /** server can signal commands to drone? */
  final case class DroneCommand()

  final case class Position(lat: Double, long: Double)

  final case class ServerCommand() // TODO do we need those?

  final case class Image(droneId: Int, imageId: Long, date: Date, position: Position, pieceResolution: Int, pieces: Array[Array[Int]])
  
  // additional things -----
  implicit final def jsonByteStringUnmarshaller[T](implicit u: FromEntityUnmarshaller[Json]): FromByteStringUnmarshaller[T] =
    Unmarshaller.strict[ByteString, Json] {
      case ByteString.empty => throw Unmarshaller.NoContentException
      case data => jawn.parseByteBuffer(data.asByteBuffer).fold(throw _, identity)
    }
    .map(it => ???)
  
}

object Domain extends Domain
object JsonDomain extends Domain with FailFastCirceSupport with AutoDerivation 
object CsvDomain extends Domain {
  // FIXME
} 
