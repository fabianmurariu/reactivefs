package com.bytes32.rxfs.core

import java.nio.channels.CompletionHandler
import java.util
import java.util.concurrent.{TimeUnit, AbstractExecutorService, ExecutorService}

import scala.concurrent.forkjoin.ForkJoinPool
import scala.concurrent.{ExecutionContextExecutorService, ExecutionContext, Promise}
import scala.language.implicitConversions
import scala.util.Success

object JavaConversions {
  implicit def Promise2CompletionHandler[V, A](promise: Promise[(V, A)]): CompletionHandler[V, A] = {
    new CompletionHandler[V, A] {
      override def completed(result: V, attachment: A): Unit = {
        promise.complete(Success((result, attachment)))
      }

      override def failed(exc: Throwable, attachment: A): Unit = {
        promise.failure(exc)
      }
    }
  }

  implicit def Promise2CompletionHandlerJavaInteger[A](promise: Promise[(Int, A)]): CompletionHandler[Integer, A] = {
    new CompletionHandler[Integer, A] {
      override def completed(result: Integer, attachment: A): Unit = {
        promise.complete(Success((result, attachment)))
      }

      override def failed(exc: Throwable, attachment: A): Unit = {
        promise.failure(exc)
      }
    }
  }

  implicit def javaIntegerTuple2Scala[A](value: (Integer, A)): (Int, A) = (value._1.toInt, value._2)

  implicit def forkJoinPool2ExecutionContext(exec: ForkJoinPool): ExecutionContext = {
    new ExecutionContext {
      override def reportFailure(cause: Throwable): Unit = ExecutionContext.defaultReporter(cause)
      override def execute(runnable: Runnable): Unit = exec.execute(runnable)
    }
  }

}
