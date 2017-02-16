package io.akka.sample

import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.scaladsl.Flow
import akka.stream.stage._

import scala.collection.immutable

object CsvSupport {

  def takeColumns(columnNames: immutable.Set[String]) = 
    Flow.fromGraph(new GraphStage[FlowShape[String, immutable.Iterable[String]]] {
    val in = Inlet[String]("CsvSupport.takeColumns.in")
    val out = Outlet[immutable.Iterable[String]]("CsvSupport.takeColumns.out")

    override def shape = FlowShape(in, out)

    override def createLogic(inheritedAttributes: Attributes) = new GraphStageLogic(shape) with StageLogging
      with InHandler with OutHandler {

      override def onPush(): Unit = {
        val headersLine = grab(in)
        val columns = headersLine.split(",").map(_.trim)

        val columnIndexes = columns.zipWithIndex.filter(it => columnNames contains it._1).map(_._2)
        becomeFilterColumns(columnIndexes)
      }

      override def onPull(): Unit = pull(in)

      setHandlers(in, out, this)

      def becomeFilterColumns(columnIndexes: Array[Int]): Unit = {
        log.info("Selecting columns: {}, indexes: {}", columnNames.toList, columnIndexes.toList)

        if (columnIndexes.isEmpty) completeStage()
        else setHandler(in, new InHandler {
          override def onPush() = {
            // TODO sub-optimal
            val data = grab(in).split(",").zipWithIndex collect {
              case (item, colId) if columnIndexes contains colId => item.trim
            }

            push(out, data.toList)
          }
        })

        pull(in)
      }

    }

  })
}
