package org.h3nk3.braces.backend

import akka.actor.{ActorSystem, PoisonPill}
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings, ClusterSingletonProxy, ClusterSingletonProxySettings}
import org.h3nk3.braces.backend.DroneManager.{StartDrones, SurveillanceArea}
import org.h3nk3.braces.domain.Domain.DronePosition

import scala.io.StdIn

object Main {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("braces-backend")
    bootstrap(system)
    println(s"Backend server running\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    system.terminate()
  }

  def bootstrap(system: ActorSystem): Unit = {
    val x = system.actorOf(
      ClusterSingletonManager.props(
        singletonProps = DroneManager.props,
        terminationMessage = PoisonPill,
        settings = ClusterSingletonManagerSettings(system)),
      "droneManager")

    // Look up the cluster singleton and send start message to it
    val droneManagerProxy = system.actorOf(ClusterSingletonProxy.props("/user/droneManager", ClusterSingletonProxySettings(system)), "droneManagerProxy")

    val saConf = system.settings.config.getConfig("braces.surveillance-area")

    // This simulates some start up process of the system in that we "create" drones.
    // In reality drones should register themselves when they start up.
    // Since this is a simplified app we do not care about minor logical mishaps.
    droneManagerProxy ! StartDrones(
          SurveillanceArea(
            DronePosition(saConf.getDouble("upper-left-lat"), saConf.getDouble("upper-left-long")),
            DronePosition(saConf.getDouble("lower-right-lat"), saConf.getDouble("lower-right-long"))),
          system.settings.config.getInt("braces.number-of-drones")
        )
  }
}

