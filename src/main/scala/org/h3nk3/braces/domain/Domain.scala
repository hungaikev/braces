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
import org.h3nk3.braces.backend.DroneManager.SurveillanceArea
import spray.json.{DefaultJsonProtocol, JsObject, JsString, JsValue, RootJsonFormat}

/**
 * Always use as: `import org.h3nk3.braces._`
 * in order to enable automatic marshalling via Circe.
 */

  case class Base(id: String, position: Position)

  case class DroneCommandError(string: String) 
  
  // Events
  trait BracesEvent

  case class InitializeClient(clientId: String) extends BracesEvent
  case class DroneData(id: Int, status: DroneStatus, position: Position, velocity: Double, direction: Int, batteryPower: Int) extends Serializable

  trait DroneStatus extends Serializable
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

  /** Commands set to the field-deployed Drones */
  trait DroneCommand
  final case class SurveilArea(area: SurveillanceArea) extends DroneCommand

  final case class Position(lat: Double, long: Double) extends Serializable
