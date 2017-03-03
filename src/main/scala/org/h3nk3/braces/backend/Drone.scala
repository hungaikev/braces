package org.h3nk3.braces.backend

case class Drone(id: String, status: DroneStatus, info: DroneInfo)

trait DroneStatus
case object Charging extends DroneStatus
case object Ready extends DroneStatus
case object Operating extends DroneStatus
case object Maintenance extends DroneStatus
case object Stopped extends DroneStatus

case class DroneInfo(position: Position, velocity: Double, direction: Int, batteryPower: Int)
case class Position(lat: Double, long: Double)