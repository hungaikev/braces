/*
 * Copyright © 2015–2017 Lightbend, Inc. All rights reserved.
 * No information contained herein may be reproduced or transmitted in any form
 * or by any means without the express written permission of Lightbend, Inc.
 */
package org.h3nk3.braces.backend

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings, ShardRegion}
import org.h3nk3.braces.backend.DroneActor.DroneInfo
import org.h3nk3.braces.domain.Domain.DronePosition

object DroneManager {
  def props: Props = Props[DroneManager]
  case class SurveillanceArea(upperLeft: DronePosition, lowerRight: DronePosition)
  case class StartDrones(area: SurveillanceArea, numberOfDrones: Int)
  case object StopDrones
}

class DroneManager extends Actor with ActorLogging {
  import DroneManager._

  def readyState: Receive = {
    case StartDrones(area, numberOfDrones) =>
      val dividedAreas: List[SurveillanceArea] = divideAreas(area, numberOfDrones)
      (1 to numberOfDrones).foreach { nbr =>
        ClusterSharding(context.system).start(
          typeName = "Drone",
          entityProps = DroneActor.props(nbr, dividedAreas(nbr - 1)),
          settings = ClusterShardingSettings(context.system),
          extractEntityId = extractEntityId,
          extractShardId = extractShardId
        )
      }

      log.info("Drones started. Switching to Running State.")
      readyState
  }

  def runningState: Receive = {
    case StopDrones =>
      log.info("Drones stopped. Switching to Ready State.")
      readyState
  }

  def receive: Receive = readyState

  // Try to produce a uniform distribution, i.e. same amount of entities in each shard.
  // As a rule of thumb, the number of shards should be a factor ten greater than the planned maximum number of cluster nodes.
  val numberOfShards = 100

  def extractShardId: ShardRegion.ExtractShardId = {
    case DroneInfo(id, _, _, _, _) => (id % numberOfShards).toString
  }

  def extractEntityId: ShardRegion.ExtractEntityId = {
    case msg @ DroneInfo(id, _, _, _, _) => (id.toString, msg)
  }

  /*
   * Yes, this is a very naive dividing function. It just splits the min/max latitude into equals parts based on number of drones.
   * In reality it should take into account range of drones, position/base of drones, etc.
   * This will suffice for our example code base though!
   */
  def divideAreas(area: SurveillanceArea, nbrDrones: Int): List[SurveillanceArea] = {
    val latPerDrone = (area.lowerRight.lat - area.upperLeft.lat) / nbrDrones
    (1 to nbrDrones).foldLeft(List.empty[SurveillanceArea])((l, n) =>
      SurveillanceArea(
        DronePosition(area.upperLeft.lat + latPerDrone * (n - 1), area.upperLeft.long),
        DronePosition(area.upperLeft.lat + latPerDrone * n, area.lowerRight.long)) +: l
    )
  }
}
