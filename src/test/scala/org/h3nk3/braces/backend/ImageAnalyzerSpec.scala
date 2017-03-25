package org.h3nk3.braces.backend

import java.util.Date

import akka.actor.ActorSystem
import org.scalatest.{Matchers, WordSpecLike}
import akka.testkit.{TestActorRef, TestKit}
import com.typesafe.config.ConfigFactory
import org.h3nk3.braces.AkkaSpec
import org.h3nk3.braces.backend.ImageAnalyzerActor.SharkIdentified
import org.h3nk3.braces.domain.Domain

object ImageAnalyzerSpec {
  val config =
    """
      |braces {
      |  image-processing-simulation-sleep = 0
      |}
    """.stripMargin
}

class ImageAnalyzerSpec extends AkkaSpec(ImageAnalyzerSpec.config) {
  "ImageAnalyzer" should {
    val droneId = "id123"
    val date = new Date()
    val position = Domain.DronePosition(10.0, 10.0)
    val pieceResolution = 1

    "not react on non shark images" in {
      val image = Image(droneId, 1L, date, position, pieceResolution, Array(Array(1,2,3), Array(4,5,6), Array(7,8,9)))
      val imageAnalyzer = TestActorRef[ImageAnalyzerActor].underlyingActor
      imageAnalyzer.analyzeImage(image) should equal(Set.empty[SharkIdentified])
    }

    "identify multiple sharks" in {
      val image = Image(droneId, 1L, date, position, pieceResolution, Array(Array(13,2,3), Array(4,5,6), Array(7,8,13)))
      val imageAnalyzer = TestActorRef[ImageAnalyzerActor].underlyingActor
      imageAnalyzer.analyzeImage(image) should contain only(SharkIdentified(image, (0,0)), SharkIdentified(image, (2,2)))
    }
  }

}
