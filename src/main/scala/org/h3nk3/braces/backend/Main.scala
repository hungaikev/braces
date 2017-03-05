package org.h3nk3.braces.backend

import akka.actor.{ActorSystem, PoisonPill}
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings}

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
    system.actorOf(
      ClusterSingletonManager.props(
        singletonProps = DroneManager.props,
        terminationMessage = PoisonPill,
        settings = ClusterSingletonManagerSettings(system)),
       name = "droneManager"
      )
  }
}

