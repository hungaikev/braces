package org.h3nk3.braces.domain

import java.util.Date

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.unmarshalling.{FromByteStringUnmarshaller, FromEntityUnmarshaller, Unmarshaller}
import akka.http.scaladsl.util.FastFuture
import akka.util.ByteString
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.{Decoder, Json, jawn}
import io.circe.export.Exported
import io.circe.generic.AutoDerivation
import spray.json.{DefaultJsonProtocol, JsObject, JsString, JsValue, RootJsonFormat}

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

  trait DroneStatus extends Serializable {
    override def toString = super.toString.dropRight(1)
  }
  object DroneStatus {
    def fromString(s: String): DroneStatus = s match {
      case "Charging"    => Charging
      case "Ready"       => Ready
      case "Operating"   => Operating
      case "Maintenance" => Maintenance
      case "Stopped"     => Stopped
    }
  }
  case object Charging    extends DroneStatus
  case object Ready       extends DroneStatus
  case object Operating   extends DroneStatus
  case object Maintenance extends DroneStatus
  case object Stopped     extends DroneStatus

  /** Commands set to the field-deployed DroneClients */
  trait DroneClientCommand
  final case class SurveilArea(upperLeft: Position, lowerRight: Position) extends DroneClientCommand

  final case class Position(lat: Double, long: Double)

  // additional things -----
  implicit final def jsonByteStringUnmarshaller[T](implicit u: FromEntityUnmarshaller[Json]): FromByteStringUnmarshaller[T] =
    Unmarshaller.strict[ByteString, Json] {
      case ByteString.empty => throw Unmarshaller.NoContentException
      case data => jawn.parseByteBuffer(data.asByteBuffer).fold(throw _, identity)
    }
    .map(it => ???)
  
}

object Domain extends Domain
