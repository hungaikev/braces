package org.h3nk3.braces.backend

import akka.actor.{Actor, ActorLogging}

import org.h3nk3.braces.domain.Domain._

object ImageAnalyzerActor {
  case class SharkIdentified(image: Image, sharkPosition: (Int, Int))
}

class ImageAnalyzerActor extends Actor with ActorLogging {
  import ImageAnalyzerActor._

  val sleepSimulation = context.system.settings.config.getLong("braces.image-processing-simulation-sleep")

  def receive = {
    case image: Image => sender ! analyzeImage(image)
  }

  /*
  * The actual analysis of the images are not part of this exercise...
  * Instead sharks are represented by the dangerous number 13. If there are any 13s amongst the arrays passed in it is considered to be a shark. Simple as! :)
  * Since analyzing images normally requires some CPU cycles we simulate this process by using Thread.sleep
  */
  def analyzeImage(image: Image): Set[SharkIdentified] = {
    var result = Set.empty[SharkIdentified]
    var pieceRow = 0
    image.pieces foreach { rowPieces =>
      var pieceColumn = 0
      rowPieces foreach { piece =>
        // Remember that 13 means that we have found a shark
        if (piece == 13) {
          result += SharkIdentified(image, (pieceRow, pieceColumn))
        }
        pieceColumn += 1
      }

      pieceRow += 1
      // Simulates work load
      Thread.sleep(sleepSimulation)
    }

    result
  }
}
