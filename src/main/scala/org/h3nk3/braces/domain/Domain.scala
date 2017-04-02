package org.h3nk3.braces.domain

import java.util.Date

import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.unmarshalling.{FromByteStringUnmarshaller, FromEntityUnmarshaller, Unmarshaller}
import akka.http.scaladsl.util.FastFuture
import akka.util.ByteString
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.{Decoder, Json, jawn}
import io.circe.export.Exported
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

  case class DroneData(id: Int, status: DroneStatus, position: DronePosition, velocity: Double, direction: Int, batteryPower: Int)

  trait DroneStatus extends Serializable
  case object Charging    extends DroneStatus
  case object Ready       extends DroneStatus
  case object Operating   extends DroneStatus
  case object Maintenance extends DroneStatus
  case object Stopped     extends DroneStatus

  /** server can signal commands to drone? */
  final case class DroneCommand()

  final case class DronePosition(lat: Double, long: Double)

  final case class ServerCommand() // TODO do we need those?

  final case class Image(droneId: Int, imageId: Long, date: Date, position: DronePosition, pieceResolution: Int, pieces: Array[Array[Int]])
  
  // additional things -----
  // TODO for entity streaming; propose inclusion of this in Akka-HTTP-json
  implicit final def makeIt(): FromByteStringUnmarshaller[DroneInfo] = ???
  
  implicit final def jsonByteStringUnmarshaller[T](decoder: Decoder[T]): FromByteStringUnmarshaller[T] =
    Unmarshaller.strict[ByteString, Json] {
      case ByteString.empty => throw Unmarshaller.NoContentException
      case data => jawn.parseByteBuffer(data.asByteBuffer).fold(throw _, identity)
    }
    .map(decoder.decodeJson)
    .map(_.right.get) // TODO make nicer
  
}

object Domain extends Domain
