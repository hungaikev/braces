package io.akka.sample

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.typesafe.config.Config
import io.akka.sample.ml.IoTDeepLearningDetection
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

class TestingStreamsSpec extends WordSpec with Matchers 
  with ScalaFutures
  with IoTDeepLearningDetection {

  implicit val sys = ActorSystem("TestingStreams")
  implicit val mat = ActorMaterializer()

  override def config: Config = sys.settings.config

  "Detection" should {
    "detect no anomally in stable stream" in {
      // given: 
      Source.repeat(0.002).take(8000)
        // when:
        .via(this.scoringFlow)
        .runForeach { score =>
          // then: 
          score.isOk should ===(true)
        }
    }
    
    "detect anomally in stable stream" in {
      val allWasOk = 
      // given: 
        Source.cycle(() => (List.fill(1000)(0.002) ++ List.fill(200)(111.0)).iterator)
        .take(8000)
        // when:
        .via(this.scoringFlow)
        .runFold(true)(_ && _.isOk)
      
      // then:
      
      allWasOk.futureValue should === (false)
    }
  }

}
