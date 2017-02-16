package io.akka.sample

import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import io.akka.sample.FeedFromFileApp.getClass

trait AkkaApp extends App {

  implicit val system = ActorSystem(Logging.simpleName(getClass).dropRight(1))
  implicit val config = system.settings.config
  implicit val materializer = ActorMaterializer()
}
