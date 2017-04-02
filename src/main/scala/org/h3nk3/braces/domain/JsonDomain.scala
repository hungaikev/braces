package org.h3nk3.braces.domain

import akka.event.Logging
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.{Marshaller, Marshalling}
import akka.http.scaladsl.model.ws
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.util.ByteString
import org.h3nk3.braces.backend.DroneManager.SurveillanceArea
import org.h3nk3.braces.domain.Domain.SurveilArea
import spray.json.{DefaultJsonProtocol, JsObject, JsString, JsValue, JsonReader, JsonWriter, RootJsonFormat}

import scala.concurrent.Future

// object JsonDomain extends Domain with FailFastCirceSupport with AutoDerivation // explain trade-off
trait JsonDomain extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val DroneStatusFormat: RootJsonFormat[Domain.DroneStatus] = new RootJsonFormat[Domain.DroneStatus] {
    override def read(json: JsValue): Domain.DroneStatus = {
      json.asJsObject.fields("status") match {
        case JsString(name) => Domain.DroneStatus.fromString(name)
      }
    }
    override def write(obj: Domain.DroneStatus): JsValue =
      JsObject("status" -> JsString(obj.toString))
  }
  implicit val DronePositionFormat = jsonFormat2(Domain.Position)
  implicit val DroneCommandFormat = new RootJsonFormat[Domain.DroneCommand] {
    override def read(json: JsValue): Domain.DroneCommand = {
      val o = json.asJsObject
      o.fields("type") match {
        case JsString("SurveilArea") => Domain.SurveilArea(
          area = {
            val a = o.fields("area").asJsObject
            SurveillanceArea(
              DronePositionFormat.read(a.fields("upperLeft")),
              DronePositionFormat.read(a.fields("lowerRight"))
            )
          } 
        )
      }
    }
    override def write(obj: Domain.DroneCommand): JsValue = {
      obj match {
        case Domain.SurveilArea(area) =>
          JsObject(
            "type" -> JsString(Logging.simpleName(obj.getClass)),
            "area" -> JsObject(
              "upperLeft" -> DronePositionFormat.write(area.upperLeft),
              "lowerRight" -> DronePositionFormat.write(area.lowerRight)
            )
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
  implicit def MessagesMarshalling[T](implicit writer: JsonWriter[T]): Marshaller[T, ws.Message] =
    Marshaller[T, ws.Message] { implicit ec => value =>  
        Future.successful(Marshalling.Opaque { () =>
          ws.TextMessage(writer.write(value).prettyPrint)
        } :: Nil)
      }
} 
object JsonDomain extends JsonDomain

