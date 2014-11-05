package com.bytes32.rxfs.core.iter

import java.nio.ByteBuffer
import java.nio.file.Paths
import java.nio.file.StandardOpenOption.READ

import com.bytes32.rxfs.core.FutureWrapper
import com.bytes32.rxfs.core.io.RxAsynchronousFileChannel
import com.bytes32.rxfs.core.op.ReadOp
import play.api.libs.iteratee.{Cont, Input, Iteratee, Enumerator}
import play.api.libs.iteratee.Enumerator.TreatCont1

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.forkjoin.ForkJoinPool

trait AsyncReader extends (ReadOp => AsyncReader) with FutureWrapper[Option[(Array[Byte], Int)]] {
  val position: Long
  val chunkSize: Int
}

case class Reader(position: Long = 0, chunkSize: Int = 8 * 1024,
                  inner: Future[Option[(Array[Byte], Int)]] = Future.successful(None)) extends AsyncReader {

  override def apply(op: ReadOp): Reader = {
    //TODO: get rid of this, return the buffer from op.read
    val readBuffer = new Array[Byte](chunkSize)
    val readFuture = op.read(ByteBuffer.wrap(readBuffer), position)
      .map {
      case (-1, _) => None
      case (`chunkSize`, _) => Some((readBuffer, chunkSize))
      case (readBytes, _) =>
        val dest = new Array[Byte](readBytes)
        Array.copy(readBuffer, 0, dest, 0, readBytes)
        Some((dest, readBytes))
    }(op.executor)
    Reader(position + chunkSize, chunkSize, readFuture)
  }
}

object Readers {
  def fromFile(file: String, chunkSize: Int = 1024 * 8)
              (implicit forkJoinPool: ForkJoinPool): AsyncReader = {
    implicit val channel = RxAsynchronousFileChannel(Paths.get(file), READ)
    val reader = Reader()
    reader(channel)
  }

  def unfoldM[S, E](s: S)(f: S => Future[Option[(S, E)]])(implicit ec: ExecutionContext): Enumerator[E] =
    Enumerator.checkContinue1(s)(new TreatCont1[E, S] {
      def apply[A](loop: (Iteratee[E, A], S) => Future[Iteratee[E, A]], s: S, k: (Input[E]) => Iteratee[E, A]): Future[Iteratee[E, A]] = {
        f(s).flatMap {
          case Some((newS, e)) => loop(k(Input.El(e)), newS)
          case None => Future.successful(Cont(k))
        }
      }
    })

  def toEnumerator(file: String, chunkSize: Int = 1024 * 8)
                  (implicit forkJoinPool: ForkJoinPool): Enumerator[Array[Byte]] = {
    implicit val channel = RxAsynchronousFileChannel(Paths.get(file), READ)

    val empty = Reader(chunkSize = chunkSize)
    val enumerator: Enumerator[Array[Byte]] = unfoldM[Reader, Array[Byte]](empty(channel)) {
      (current: Reader) =>
        current.map {
          case Some((bytes, size)) => Some(current(channel) -> bytes)
          case None => None
        }(channel.executor)
    }(channel.executor)
    enumerator
  }
}


