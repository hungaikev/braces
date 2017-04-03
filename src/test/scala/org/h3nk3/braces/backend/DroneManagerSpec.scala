package org.h3nk3.braces.backend

import akka.actor.ActorRef
import org.h3nk3.braces.AkkaSpec
import org.h3nk3.braces.backend.DroneManager.{DronesStopped, Initiating, SurveillanceArea}
import org.h3nk3.braces.domain.Position
import org.scalatest.{Matchers, WordSpecLike}

import scala.concurrent.duration._

class DroneManagerSpec 
  extends AkkaSpec
  with WordSpecLike with Matchers {

  "DroneManager" should {
    "divide surveillance area based on number of drones" in {
      val droneManagerActor: ActorRef = system.actorOf(DroneManager.props, "manager")

      val upperLeftDronePosition = Position(0.0, 0.0)
      val lowerRightDronePosition = Position(10.0, 10.0)
      
      val area = SurveillanceArea(upperLeftDronePosition, lowerRightDronePosition)

      droneManagerActor ! DroneManager.Initiate(area, 10)
      expectMsg(Initiating)

      expectNoMsg(100.millis)

      droneManagerActor ! DroneManager.StopDrones
      expectMsg(DronesStopped)
    }
  }
}

