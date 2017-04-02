package org.h3nk3.braces.backend

import akka.actor.{Actor, ActorSystem, Props}
import akka.actor.Actor.Receive
import akka.cluster.singleton.{ClusterSingletonProxy, ClusterSingletonProxySettings}
import org.h3nk3.braces.backend.DroneActor.InitDrone

object DroneClientConnectionHub {
  
  def proxyProps(system: ActorSystem) =
    ClusterSingletonProxy.props(s"/user/$name", ClusterSingletonProxySettings(system))
  
  def name = "droneClientConnectionHub"
  
  def props(): Props =
    Props(new DroneClientConnectionHub)
  
  case class SendCommand(id: String, )
}

/** Manages connections and pushes commands to field-deployed DroneClients */
class DroneClientConnectionHub extends Actor {
  
  var droneClientOut = 
  var droneClientIn = 
  
  override def receive: Receive = {
    case InitDrone => 
  }
}
