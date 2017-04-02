/*
 * Copyright © 2015–2017 Lightbend, Inc. All rights reserved.
 * No information contained herein may be reproduced or transmitted in any form
 * or by any means without the express written permission of Lightbend, Inc.
 */
package org.h3nk3.braces.backend

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props}
import akka.cluster.singleton.{ClusterSingletonProxy, ClusterSingletonProxySettings}
import org.h3nk3.braces.domain.Domain
import org.h3nk3.braces.domain.Domain._

object DroneManager {
  def props: Props = Props[DroneManager]
  case class SurveillanceArea(upperLeft: Position, lowerRight: Position, coverage: Int = 0) extends Serializable
  case class Initiate(area: SurveillanceArea, numberOfDrones: Int) extends Serializable
  case class DroneStarted(actorRef: ActorRef) extends Serializable
  case class DroneStopped(actorRef: ActorRef) extends Serializable
  case class DroneTaskFinished(actorRef: ActorRef) extends Serializable
  case object StopDrones extends Serializable
  
  def singletonProxyProps(system: ActorSystem): Props = 
    ClusterSingletonProxy.props("/user/droneManager", ClusterSingletonProxySettings(system))
}

class DroneManager extends Actor with ActorLogging {
  import DroneManager._

  var availableDrones = Set.empty[ActorRef]
  var dividedAreas = Set.empty[SurveillanceArea]
  var workingDrones = Map.empty[ActorRef, SurveillanceArea]
  var standbyDrones = Set.empty[ActorRef]
  var id = 0

  def readyState: Receive = {
    case Initiate(area, numberOfDrones) =>
      dividedAreas = divideAreas(area, numberOfDrones)
      log.info("DroneManager initiated. Switching to Running State.")
      context.become(runningState)
    case s =>
      log.warning(s"Unexpected command '$s' in state ready.")
  }

  def runningState: Receive = {
    case DroneStarted(actorRef) =>
      availableDrones = availableDrones + actorRef
      assignWork(actorRef)
    case DroneStopped(actorRef) =>
      handleStoppedDrone(actorRef)
    case DroneTaskFinished(actorRef) =>
      workingDrones = workingDrones - actorRef
    case StopDrones =>
      // Improvement - we should instruct the drones to go back to base before just removing them like this...
      log.info(s"Stopping all ${availableDrones.size} drones.")
      availableDrones foreach { _ ! PoisonPill }
      // todo standby drones
      log.info("Drones stopped. Switching to Ready State.")
      context.become(readyState)
    case s =>
      log.warning(s"Unexpected command '$s' in state running.")
  }

  def receive: Receive = readyState

  /*
   * Yes, this is a very naive dividing function. It just splits the min/max latitude into equals parts based on number of drones.
   * In reality it should take into account range of drones, position/base of drones, etc.
   * This will suffice for our example code base though!
   */
  def divideAreas(area: SurveillanceArea, nbrDrones: Int): Set[SurveillanceArea] = {
    val latPerDrone = (area.lowerRight.lat - area.upperLeft.lat) / nbrDrones
    (1 to nbrDrones).foldLeft(Seq.empty[SurveillanceArea])((l, n) =>
      SurveillanceArea(
        Position(area.upperLeft.lat + latPerDrone * (n - 1), area.upperLeft.long),
        Position(area.upperLeft.lat + latPerDrone * n, area.lowerRight.long)) +: l
    ).toSet
  }

  def assignWork(droneShadow: ActorRef): Unit = {
    if (dividedAreas.nonEmpty) {
      val area = dividedAreas.head
      id += 1
      droneShadow ! Domain.SurveilArea(area)
      workingDrones = workingDrones + (droneShadow -> area)

      dividedAreas = dividedAreas.tail
    } else
      standbyDrones = standbyDrones + droneShadow
  }

  def handleStoppedDrone(actorRef: ActorRef): Unit = {
    // Remove drone from available ones
    availableDrones = availableDrones - actorRef

    // Put back the area into the surveillance set
    workingDrones.get(actorRef) map { area: SurveillanceArea => dividedAreas += area}

    // Assign the work to a new actor
    if (standbyDrones.nonEmpty) {
      assignWork(standbyDrones.head)
      standbyDrones = standbyDrones.tail
    }
  }
}
