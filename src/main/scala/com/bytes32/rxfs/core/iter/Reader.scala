package com.bytes32.rxfs.core.iter

import java.nio.file.Paths
import java.nio.file.StandardOpenOption.READ

import com.bytes32.rxfs.core.io.{AsyncIO, ReadIO, RxAsynchronousFileChannel}
import com.bytes32.rxfs.core.op.ReadOp
import play.api.libs.iteratee.Enumerator.TreatCont1
import play.api.libs.iteratee.{Cont, Enumerator, Input, Iteratee}

import scala.concurrent.forkjoin.ForkJoinPool
import scala.concurrent.{ExecutionContext, Future}

case class Reader(position: Long = 0, chunkSize: Int = 8 * 1024,
                  inner: Future[Option[(Array[Byte], Int)]] = Future.successful(None)) extends ReadIO {

  override def apply(op: ReadOp): Reader = {
    val readFuture = op.read(new Array[Byte](chunkSize), position)
      .map {
      case (-1, _) => None
      case (`chunkSize`, bytes) => Some((bytes, chunkSize))
      case (readBytes, bytes) =>
        val dest = new Array[Byte](readBytes)
        Array.copy(bytes, 0, dest, 0, readBytes)
        Some((dest, readBytes))
    }(op.executor)
    Reader(position + chunkSize, chunkSize, readFuture)
  }
}

object Readers {
  def fromFile(file: String, chunkSize: Int = 1024 * 8)
              (implicit forkJoinPool: ForkJoinPool): AsyncIO = {
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

    val enumerator: Enumerator[Array[Byte]] = unfoldM[Reader, Array[Byte]](Reader(0, chunkSize)(channel)) {
      current => current.map {
          case Some((bytes, size)) => Some(current(channel) -> bytes)
          case None => None
        }(channel.executor)
    }(channel.executor)
    enumerator
  }
}


