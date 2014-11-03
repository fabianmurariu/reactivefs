package com.bytes32.rxfs.core.iter

import java.nio.file.Paths
import java.nio.file.StandardOpenOption.{WRITE, CREATE}

import com.bytes32.rxfs.core.io.RxAsynchronousFileChannel
import play.api.libs.iteratee.{Enumerator, Step, Iteratee}

import scala.concurrent.forkjoin.ForkJoinPool
import scala.concurrent.{ExecutionContext, Future}
import com.bytes32.rxfs.core.JavaConversions.forkJoinPool2ExecutionContext

object Writer {

  def apply[A](file: String, chunkSize: Int = 1024 * 8)
              (implicit forkJoinPool: ForkJoinPool): Enumerator[Array[Byte]] = {
    val channel = RxAsynchronousFileChannel(Paths.get(file), WRITE, CREATE)
    Enumerator.generateM[Array[Byte]]({
      ???
    })(forkJoinPool)
  }

}
