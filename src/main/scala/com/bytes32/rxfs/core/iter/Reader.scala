package com.bytes32.rxfs.core.iter

import java.nio.ByteBuffer
import java.nio.file.Paths
import java.nio.file.StandardOpenOption.READ

import com.bytes32.rxfs.core.JavaConversions.forkJoinPool2ExecutionContext
import com.bytes32.rxfs.core.io.RxAsynchronousFileChannel
import com.bytes32.rxfs.core.op.ReadOp
import play.api.libs.iteratee.Enumerator.TreatCont0
import play.api.libs.iteratee.Step.Done
import play.api.libs.iteratee._

import scala.concurrent._
import scala.concurrent.duration.Duration
import scala.concurrent.forkjoin.ForkJoinPool
import scala.util.Try

class Reader(position: Long = 0L, chunkSize: Int = 8 * 1024)
            (implicit op: ReadOp, executor: ForkJoinPool = new ForkJoinPool(5))
  extends Future[Array[Byte]] {


  lazy val readBuffer = new Array[Byte](chunkSize)
  lazy val read: Future[Array[Byte]] = op.read(ByteBuffer.wrap(readBuffer), position)
    .map { case (readBytes, attachment) => readBuffer}(executor)

  def next(pos: Long = position + chunkSize, size: Int = chunkSize): Reader = {
    new Reader(pos, size)(op, executor)
  }

  override def onComplete[U](f: (Try[Array[Byte]]) => U)(implicit executor: ExecutionContext): Unit = read.onComplete(f)

  override def isCompleted: Boolean = read.isCompleted

  override def value: Option[Try[Array[Byte]]] = read.value

  @throws[Exception](classOf[Exception])
  override def result(atMost: Duration)(implicit permit: CanAwait): Array[Byte] = read.result(atMost)

  @throws[InterruptedException](classOf[InterruptedException])
  @throws[TimeoutException](classOf[TimeoutException])
  override def ready(atMost: Duration)(implicit permit: CanAwait): Reader.this.type = {
    result(atMost)(permit)
    if (this.read.isCompleted) this
    else throw new TimeoutException("Futures timed out after [" + atMost + "]")
  }

}

object Reader {
  def readFile(file: String, chunkSize: Int = 1024 * 8)
              (implicit forkJoinPool: ForkJoinPool): Reader = {
    implicit val channel = RxAsynchronousFileChannel(Paths.get(file), READ)
    new Reader(0, chunkSize)
  }

  def toEnumerator(file: String, chunkSize: Int = 1024 * 8)
                  (implicit forkJoinPool: ForkJoinPool): Enumerator[Array[Byte]] = {
    implicit val channel = RxAsynchronousFileChannel(Paths.get(file), READ)
    val badReader = new Reader(0, chunkSize)
    Enumerator.repeatM{
      badReader.next()
    }
//    Enumerator.checkContinue0[Array[Byte]](new TreatCont0[Array[Byte]] {
//      override def apply[A](loop: (Iteratee[Array[Byte], A]) => Future[Iteratee[Array[Byte], A]],
//                            k: (Input[Array[Byte]]) => Iteratee[Array[Byte], A]): Future[Iteratee[Array[Byte], A]] = ???
//    })
  }
}


