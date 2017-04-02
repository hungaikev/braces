package org.h3nk3.braces.domain

import akka.event.Logging
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.ws
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.util.ByteString
import spray.json.{DefaultJsonProtocol, JsObject, JsString, JsValue, JsonReader, RootJsonFormat}

import scala.concurrent.Future

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
  implicit val DronePositionFormat = jsonFormat2(Domain.Position)
  implicit val DroneClientCommandFormat = new RootJsonFormat[Domain.DroneClientCommand] {
    override def read(json: JsValue): Domain.DroneClientCommand = {
      val o = json.asJsObject
      o.fields("type") match {
        case JsString("SurveilArea") => Domain.SurveilArea(
          upperLeft = DronePositionFormat.read(o.fields("upperLeft")),
          lowerRight = DronePositionFormat.read(o.fields("lowerRight"))
        )
      }
    }
    override def write(obj: Domain.DroneClientCommand): JsValue = {
      obj match {
        case Domain.SurveilArea(upperLeft, lowerRight) =>
          JsObject(
            "type" -> JsString(Logging.simpleName(obj.getClass)),
            "upperLeft" -> DronePositionFormat.write(upperLeft),
            "lowerRight" -> DronePositionFormat.write(lowerRight)
          )
      }
    }
  }
  implicit val DroneDataFormat = jsonFormat6(Domain.DroneData)
  implicit val InitializeClientFormat = jsonFormat1(Domain.InitializeClient)
  
  implicit def MessagesUnmarshalling[T](implicit reader: JsonReader[T]): Unmarshaller[ws.Message, T] =
    Unmarshaller.withMaterializer { implicit ec => implicit mat => value =>  
      value.asBinaryMessage.asScala.dataStream.runFold(ByteString.empty)(_ ++ _) flatMap { acc =>
        import spray.json._
        Future.successful(reader.read(acc.utf8String.parseJson))
      }
    }
} 
object JsonDomain extends JsonDomain

