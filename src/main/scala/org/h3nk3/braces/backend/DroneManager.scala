/*
 * Copyright © 2015–2017 Lightbend, Inc. All rights reserved.
 * No information contained herein may be reproduced or transmitted in any form
 * or by any means without the express written permission of Lightbend, Inc.
 */
package org.h3nk3.braces.backend

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings, ShardRegion}
import org.h3nk3.braces.domain.Domain.DronePosition

object DroneManager {
  def props: Props = Props[DroneManager]
  
  case class Instructions(area: SurveillanceArea, numberOfDrones: Int)
  case class SurveillanceArea(upperRight: DronePosition, lowerLeft: DronePosition)
  case object StartDrones
  case object StopDrones
}

class DroneManager extends Actor with ActorLogging {
  import DroneManager._

  // Improvement: should be persisted to handle restart of singleton somewhere else in the cluster
  var instructions: Option[Instructions] = None

  def readyState: Receive = {
    case inst: Instructions =>
      instructions = Some(inst)
    case StartDrones =>
      instructions map { instr =>
        val dividedAreas: Map[Int, SurveillanceArea] = divideAreas(instr.area, instr.numberOfDrones)
        (1 to instr.numberOfDrones).foreach { nbr =>
          ClusterSharding(context.system).start(
            typeName = "Drone",
            entityProps = Drone.props(s"id-$nbr", dividedAreas(nbr)),
            settings = ClusterShardingSettings(context.system),
            extractEntityId = extractEntityId,
            extractShardId = extractShardId
          )
      }

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

  def extractEntityId: ShardRegion.ExtractEntityId = {
    case _ => ("", "") // FIXME
  }

  def extractShardId: ShardRegion.ExtractShardId = {
    case _ => "" // FIXME
  }

  /*
   * Yes, this is a very naive dividing function.
   * In reality it should take into account range of drones, position/base of drones, etc.
   * This will suffice for our example code base though.
   */
  def divideAreas(area: SurveillanceArea, nbrDrones: Int): Map[Int, SurveillanceArea] = {
    // FIXME
    Map.empty[Int, SurveillanceArea]
  }
}
