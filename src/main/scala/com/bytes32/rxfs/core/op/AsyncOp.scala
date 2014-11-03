package com.bytes32.rxfs.core.op

import java.nio.channels.{AsynchronousFileChannel, CompletionHandler}

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Try}

trait AsyncOp {

  val channel: AsynchronousFileChannel

  def async[FROM, A >: Null <: AnyRef, TO](op: CompletionHandler[FROM, A] => Unit)
                                          (implicit interloper: (Promise[(TO, A)]) => CompletionHandler[FROM, A]): Future[(TO, A)] = {

    val promise = Promise[(TO, A)]()
    /* deal with early failures */
    Try(op(promise)) match {
      case Failure(ex) => promise.failure(ex)
      case _ =>
    }
    promise.future
  }

}
