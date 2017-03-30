package org.h3nk3.braces.backend

import akka.actor.{ActorIdentity, ActorPath, ActorSystem, Identify, PoisonPill, Props}
import akka.cluster.sharding.ClusterSharding
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings, ClusterSingletonProxy, ClusterSingletonProxySettings}
import akka.persistence.journal.leveldb.{SharedLeveldbJournal, SharedLeveldbStore}
import akka.util.Timeout
import akka.pattern.ask
import com.typesafe.config.ConfigFactory
import org.h3nk3.braces.backend.DroneManager.{StartDrones, SurveillanceArea}
import org.h3nk3.braces.domain.Domain._

import scala.concurrent.duration._
import scala.io.StdIn

object Main {
  def main(args: Array[String]): Unit = {
    var systems = Seq.empty[ActorSystem]
    if (args.isEmpty) {
      systems = Seq(2551, 2552) map { startBackend(_) }
    } else {
      val port = args(0).toInt
      systems = Seq(startBackend(port))
    }

    Thread.sleep(5000)
    ClusterSharding(systems.head).shardRegion("Drone") ! DroneData(1, Ready, DronePosition(0.0, 0.0), 0.0, 0, 100)

    println(s"Backend server running\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    systems foreach { _.terminate() }
  }

  def startBackend(port: Int): ActorSystem = {
    val conf = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").withFallback(ConfigFactory.load())
    val system = ActorSystem("BracesBackend", conf)
    bootstrap(system, port == 2551) // only start store
    system
  }

  def bootstrap(system: ActorSystem, startThings: Boolean): Unit = {
    system.actorOf(
      ClusterSingletonManager.props(
        singletonProps = DroneManager.props,
        terminationMessage = PoisonPill,
        settings = ClusterSingletonManagerSettings(system)),
      "droneManager")

    // Start the shared local journal used in this demo
    startupSharedJournal(system, startThings, ActorPath.fromString("akka.tcp://BracesBackend@127.0.0.1:2551/user/store"))

    if (startThings) {
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

  def startupSharedJournal(system: ActorSystem, startStore: Boolean, path: ActorPath): Unit = {
    // Start the shared journal one one node (don't crash this SPOF)
    // This will not be needed with a distributed journal
    if (startStore) system.actorOf(Props[SharedLeveldbStore], "store")

    // register the shared journal
    import system.dispatcher
    implicit val timeout = Timeout(15.seconds)
    val f = (system.actorSelection(path) ? Identify(None))
    f.onSuccess {
      case ActorIdentity(_, Some(ref)) => SharedLeveldbJournal.setStore(ref, system)
      case _ =>
        system.log.error("Shared journal not started at {}", path)
        system.terminate()
    }
    f.onFailure {
      case _ =>
        system.log.error("Lookup of shared journal at {} timed out", path)
        system.terminate()
    }
  }
}

