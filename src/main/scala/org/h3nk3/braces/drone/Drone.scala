package org.h3nk3.braces.drone

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.ws._
import akka.http.scaladsl.model.ws
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ActorAttributes, ActorMaterializer}
import akka.stream.scaladsl.{CoupledTerminationFlow, Flow, Sink, Source}
import com.typesafe.config.ConfigFactory
import org.h3nk3.braces.backend.DroneManager.SurveillanceArea
import org.h3nk3.braces.backend.InputParser
import org.h3nk3.braces.domain.Domain._
import org.h3nk3.braces.domain.JsonDomain

import scala.annotation.tailrec
import scala.concurrent.duration._
import scala.io.StdIn

object Drone extends InputParser with JsonDomain {
  
  implicit val sys = ActorSystem("Drone-" + System.currentTimeMillis(), ConfigFactory.load("drone-client.conf"))
  implicit val mat = ActorMaterializer()
  val log = Logging(sys, getClass)
  import sys.dispatcher

  // Drone work related info
  @volatile private var area: Option[SurveillanceArea] = None

  var droneId: Int = 0

  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      println(Console.RED + "**** You must provide a drone id when starting the drone client. ****" + Console.RESET)
      System.exit(0)
    } else {
      droneId = args.head.toInt
      bootstrap()
      println(
        Console.GREEN + 
        s"*** Drone client [$droneId running]\n" +
        s"Type 'e|exit' to quit the application. Type 'h|help' for information." + 
        Console.RESET)
      commandLoop()
    }
  }

  @tailrec
  def commandLoop(): Unit = {
    Cmd(StdIn.readLine()) match {
      case Cmd.Help =>
        println("Available commands:")
        println("h: Help")
        println("e: Exit")
        commandLoop()
      case Cmd.Unknown(s) =>
        println(s"Unknown command: $s")
        commandLoop()
      case Cmd.Exit =>
        println("Exiting application. Bye!")
        sys.terminate()
      case Cmd.Initiate =>
        // ignore
        commandLoop()
      case Cmd.Stop =>
        // ignore
        commandLoop()
      case Cmd.AddDrone =>
        // ignore
        commandLoop()
    }
  }

  def bootstrap(): Unit = {
    val url = s"${sys.settings.config.getString("braces.http-server")}drone/$droneId"
    log.info("Connecting to: {}", url)
    Http().singleWebSocketRequest(
      WebSocketRequest(url),
      clientFlow = emitPositionAndMetrics)
  }

  def handleCommand(json: ws.Message): Unit = {
    Unmarshal(json).to[DroneCommand] map {
      case SurveilArea(area) =>
        log.info("Received command to surveil: {}", area)
        this.area = Some(area)
    }
  }

  def emitPositionAndMetrics: Flow[Message, Message, Any] =
    CoupledTerminationFlow.fromSinkAndSource(
      Sink.foreach(handleCommand),
      Source.tick(initialDelay = 1.second, interval = 1.second, tick = NotUsed)
        .map(_ => getCurrentInfo)
        .via(renderAsJson)
        .log(s"Drone-$droneId").withAttributes(ActorAttributes.logLevels(onElement = Logging.InfoLevel))
        .map(TextMessage(_))
    )

  def getCurrentInfo: DroneData = {
    def position(): Position = {
      Position(0.0, 0.0)
    }

    def velocity(): Double = {
      0.0
    }

    def direction: Int = {
      0
    }

    def batteryPower: Int = {
      100
    }

    if (area.isDefined) DroneData(droneId, Operating, position(), velocity, direction, batteryPower)
    else DroneData(droneId, Ready, Position(0.0, 0.0), 0.0, 0, 100)
  }

  val renderAsJson: Flow[DroneData, String, NotUsed] =
    Flow[DroneData]
      .mapAsync(parallelism = 1)(p => Marshal(p).to[HttpEntity].flatMap(_.toStrict(1.seconds)))
      .map(_.data.utf8String)

}
