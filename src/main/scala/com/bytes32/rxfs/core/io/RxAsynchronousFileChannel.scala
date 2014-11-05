package com.bytes32.rxfs.core.io

import java.nio.channels.AsynchronousFileChannel
import java.nio.file.{OpenOption, Path}

import com.bytes32.rxfs.core.op.{AsyncOp, LockOp, ReadOp, WriteOp}

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext
import scala.concurrent.forkjoin.ForkJoinPool
import com.bytes32.rxfs.core.JavaConversions.forkJoinPool2ExecutionContext

trait RxAsynchronousFileChannel extends LockOp with WriteOp with ReadOp with AsyncOp

object RxAsynchronousFileChannel {

  def apply(path: Path, openflags: OpenOption*)
           (implicit exec: ForkJoinPool): RxAsynchronousFileChannel = {

    new RxAsynchronousFileChannel {

      override val executor: ExecutionContext = exec

      override val channel: AsynchronousFileChannel = {
        val jOpenFlags: java.util.Set[_ <: OpenOption] = openflags.toSet[OpenOption]

        AsynchronousFileChannel.open(path, jOpenFlags, exec)
      }
    }
  }
}






