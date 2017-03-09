package org.h3nk3.braces.web

object Domain {

  case class Position(lat: Double, long: Double)

  case class Drone(id: String, position: Position)

  case class Base(id: String, position: Position)

  // Events
  trait BracesEvent

  case class InitializeClient(clientId: String) extends BracesEvent


}
