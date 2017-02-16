package io.akka.sample.model

sealed trait MachineStatus {
  def isOk: Boolean
  def value: Int
}

object MachineStatus {

  case object Ok extends MachineStatus {
    override def isOk = true
    override def value = 0
  }

  case object Misbehaving extends MachineStatus {
    override def isOk = false
    override def value = 1
  }

}

sealed trait NeedMoreData

case object NeedMoreData extends NeedMoreData
