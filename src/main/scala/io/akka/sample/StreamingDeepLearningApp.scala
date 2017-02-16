package io.akka.sample

import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.ActorMaterializer
import io.akka.sample.ml.IoTDeepLearningDetection

object StreamingDeepLearningApp extends App 
  with IoTDeepLearningDetection {
  
  implicit val system = ActorSystem(Logging.simpleName(getClass))
  implicit val config = system.settings.config
  implicit val materializer = ActorMaterializer()
  

  
  
}
