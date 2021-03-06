package com.bytes32.rxfs.core.iter

import java.io.File
import java.nio.file.Paths
import java.nio.file.StandardOpenOption._

import com.bytes32.rxfs.core.io.{RxAsynchronousFileChannel, WriteIO}
import com.bytes32.rxfs.core.op.WriteOp
import play.api.libs.iteratee.{Cont, Done, Input, Iteratee}

import scala.concurrent.forkjoin.ForkJoinPool
import scala.concurrent.{ExecutionContext, Future}

case class Writer(position: Long = 0,
                  inner: Future[Int] = Future.successful(0))
  extends WriteIO {

  override def apply(op: WriteOp, bytes: Array[Byte]): Writer = {
    val writeFuture = op.write(bytes, position).map(_._1)(op.executor)
    Writer(position + bytes.size, writeFuture)
  }

}

object Writers {

  def toIteratee(file: File)
                (implicit forkJoinPool: ForkJoinPool): Iteratee[Array[Byte], Int] = {
    implicit val channel = RxAsynchronousFileChannel(Paths.get(file.getAbsolutePath), WRITE)

    foldM[Array[Byte]](Writer(0)) {
      case (writer, bytes) => writer(channel, bytes)
    }(channel.executor)

  }

  private def foldM[E](state: Writer)(f: (Writer, E) => Writer)(implicit ec: ExecutionContext): Iteratee[E, Int] = {

    def step(s: Writer)(i: Input[E]): Iteratee[E, Int] = i match {

      case Input.EOF => Iteratee.flatten(s.map[Iteratee[E, Int]](value => Done(value, Input.EOF)))//Done(s, Input.EOF)
      case Input.Empty => Cont[E, Int](step(s))
      case Input.El(e) =>
        val newS: Writer = f(s, e)
        Cont[E, Int](step(newS))
    }
    Cont[E, Int](step(state))
  }

}