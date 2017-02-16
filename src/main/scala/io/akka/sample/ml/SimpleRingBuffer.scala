package io.akka.sample.ml

import scala.collection.mutable
import scala.collection.generic._

class SimpleRingBuffer[A](m: Int) extends mutable.Buffer[A] 
  with GenericTraversableTemplate[A, SimpleRingBuffer] 
  with mutable.BufferLike[A, SimpleRingBuffer[A]] 
  with mutable.Builder[A, List[A]] {
  private val buf = new mutable.ListBuffer[A]

  private def resize(): Unit = while (buf.size > m) buf.remove(0)

  def length = buf.length
  override def apply(n: Int): A = buf.apply(n)
  def update(n: Int, x: A) = buf.update(n, x)
  def +=(x: A): this.type = { buf.+=(x); resize(); this }
  def clear() = buf.clear();
  def +=:(x: A): this.type = { buf.+=:(x); resize(); this }
  def insertAll(n: Int, seq: scala.collection.Traversable[A]): Unit = buf.insertAll(n, seq)
  override def remove(n: Int, count: Int) = buf.remove(n, count)
  def result(): List[A] = buf.result()
  override def toList: List[A] = buf.toList
  def prependToList(xs: List[A]): List[A] = buf.prependToList(xs)
  def remove(n: Int): A = buf.remove(n)
  override def -=(elem: A): this.type = { buf.-=(elem); this }
  override def iterator = buf.iterator
  //  override def readOnly: List[A] = buf.readOnly
  override def equals(that: Any): Boolean = buf.equals(that)
  override def clone(): SimpleRingBuffer[A] = new SimpleRingBuffer(m) ++= this
  override def stringPrefix: String = "SimpleRingBuffer"
  override def companion: GenericCompanion[SimpleRingBuffer] = SimpleRingBuffer
}

object SimpleRingBuffer extends SeqFactory[SimpleRingBuffer] {
  implicit def canBuildFrom[A]: CanBuildFrom[Coll, A, SimpleRingBuffer[A]] = new GenericCanBuildFrom[A]
  def newBuilder[A]: mutable.Builder[A, SimpleRingBuffer[A]] = new mutable.GrowingBuilder(new SimpleRingBuffer[A](Int.MaxValue))
}
