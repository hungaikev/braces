package org.h3nk3.braces.backend

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit}
import com.typesafe.config.ConfigFactory
import org.h3nk3.braces.backend.DroneManager.SurveillanceArea
import org.h3nk3.braces.domain.Domain.DronePosition
import org.scalatest.{Matchers, WordSpecLike}

class DroneManagerSpec extends TestKit(ActorSystem("TestActorSystem", ConfigFactory.parseString(""))) with WordSpecLike with Matchers {

  "DroneManager" should {
    "divide surveillance area based on number of drones" in {
      val droneManagerActor = TestActorRef[DroneManager].underlyingActor

      val upperLeftDronePosition = DronePosition(0.0, 0.0)
      val lowerRightDronePosition = DronePosition(10.0, 10.0)
      val sa = SurveillanceArea(upperLeftDronePosition, lowerRightDronePosition)
      val areas1 = droneManagerActor.divideAreas(sa, 1)
      val areas2 = droneManagerActor.divideAreas(sa, 2)
      val areas4 = droneManagerActor.divideAreas(sa, 4)

      areas1.size should be(1)
      areas2.size should be(2)
      areas4.size should be(4)

      areas1.head should equal(SurveillanceArea(upperLeftDronePosition, lowerRightDronePosition))

      areas2(1) should equal(SurveillanceArea(DronePosition(0.0, 0.0), DronePosition(5.0, 10.0)))
      areas2(0) should equal(SurveillanceArea(DronePosition(5.0, 0.0), DronePosition(10.0, 10.0)))

      areas4(1) should equal(SurveillanceArea(DronePosition(5.0, 0.0), DronePosition(7.5, 10.0)))
      areas4(0) should equal(SurveillanceArea(DronePosition(7.5, 0.0), DronePosition(10.0, 10.0)))
    }
  }
}

