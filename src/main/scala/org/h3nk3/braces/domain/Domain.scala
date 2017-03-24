package org.h3nk3.braces.domain

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

object Domain extends SprayJsonSupport with DefaultJsonProtocol {

  
  case class Position(lat: Double, long: Double)
  object Position {
    implicit val Format = jsonFormat2(Position.apply)
  }

  case class Drone(id: String, position: Position)

  case class Base(id: String, position: Position)

  // Events
  trait BracesEvent

  case class InitializeClient(clientId: String) extends BracesEvent


  
}
