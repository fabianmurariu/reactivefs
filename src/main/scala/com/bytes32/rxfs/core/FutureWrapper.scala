package com.bytes32.rxfs.core

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, CanAwait, TimeoutException, Future}
import scala.util.Try

trait FutureWrapper[T] extends Future[T]{

  val inner : Future[T]

  override def onComplete[U](f: (Try[T]) => U)(implicit executor: ExecutionContext): Unit = inner.onComplete(f)
  override def isCompleted: Boolean = inner.isCompleted
  override def value: Option[Try[T]] = inner.value

  @throws[Exception](classOf[Exception])
  override def result(atMost: Duration)(implicit permit: CanAwait): T = inner.result(atMost)(permit)
  @throws[InterruptedException](classOf[InterruptedException])
  @throws[TimeoutException](classOf[TimeoutException])
  override def ready(atMost: Duration)(implicit permit: CanAwait): this.type = {
    inner.ready(atMost)(permit)
    if (inner.isCompleted) this
    else throw new TimeoutException("Futures timed out after [" + atMost + "]")
  }
}
