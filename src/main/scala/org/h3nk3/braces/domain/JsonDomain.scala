package org.h3nk3.braces.domain

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, JsObject, JsString, JsValue, RootJsonFormat}

// object JsonDomain extends Domain with FailFastCirceSupport with AutoDerivation // explain trade-off
trait JsonDomain extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val DroneStatusFormat: RootJsonFormat[Domain.DroneStatus] = new RootJsonFormat[Domain.DroneStatus] {
    override def read(json: JsValue): Domain.DroneStatus = {
      json.asJsObject.getFields("status").head match {
        case JsString(name) => Domain.DroneStatus.fromString(name)
      }
    }
    override def write(obj: Domain.DroneStatus): JsValue =
      JsObject("status" -> JsString(obj.toString.dropRight(1)))
  }
  implicit val DronePositionFormat = jsonFormat2(Domain.DronePosition)
  implicit val DroneClientCommandFormat = jsonFormat1(Domain.DroneClientCommand)
  implicit val DroneDataFormat = jsonFormat6(Domain.DroneData)
  implicit val InitializeClientFormat = jsonFormat1(Domain.InitializeClient)
} 
object JsonDomain extends JsonDomain

