package org.h3nk3.braces.backend

import akka.actor.ActorSystem

import scala.io.StdIn

object Main {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("braces-backend")
    println(s"Backend server running\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    system.terminate()
  }
}

