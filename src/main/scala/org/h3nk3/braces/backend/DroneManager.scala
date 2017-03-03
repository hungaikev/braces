/*
 * Copyright © 2015–2017 Lightbend, Inc. All rights reserved.
 * No information contained herein may be reproduced or transmitted in any form
 * or by any means without the express written permission of Lightbend, Inc.
 */
package org.h3nk3.braces.backend

import akka.actor.{Actor, ActorLogging, Props}

object DroneManager {
  def props: Props = Props[DroneManager]
  case class Instructions(area: SurveillanceArea, numberOfDrones: Int)
  case class SurveillanceArea(lat: Double, long: Double)
  case object StartDrones
  case object StopDrones
}

class DroneManager extends Actor with ActorLogging {
  import DroneManager._
  var instructions: Option[Instructions] = None

  def readyState: Receive = {
    case inst: Instructions => instructions = Some(inst)
    case StartDrones =>
      instructions map { i =>
        // TODO : Bootstrap drones, send instructions to drones
        log.info("Drones started. Switching to Running State.")
        readyState
      }
  }

  def runningState: Receive = {
    case StopDrones =>
      log.info("Drones stopped. Switching to Ready State.")
      readyState
  }

  def receive: Receive = readyState
}
