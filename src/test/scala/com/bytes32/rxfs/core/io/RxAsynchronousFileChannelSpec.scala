package com.bytes32.rxfs.core.io

import java.nio.ByteBuffer
import java.nio.channels.{FileLock, OverlappingFileLockException}
import java.nio.file.Paths
import java.nio.file.StandardOpenOption.{CREATE, READ, WRITE}
import java.util.concurrent.{ExecutorService, Executors}

import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.Await.result
import scala.concurrent.duration._
import scala.concurrent.forkjoin.ForkJoinPool
import scala.language.postfixOps
import scala.util.Random

class RxAsynchronousFileChannelSpec extends FlatSpec with Matchers with BeforeAndAfter {

  var cleanupLock: Option[FileLock] = None

  after {
    cleanupLock.map(inner => inner.release())
  }

  "RxAsynchronousFileChannel" should "exclusively lock a portion of a file" in {
    implicit val exec: ForkJoinPool = new ForkJoinPool(3)
    val channel = RxAsynchronousFileChannel(Paths.get("/tmp/asyncfile"), CREATE, WRITE, READ)

    val futureLock = channel.lock(0, 10, share = false)

    val (lock, _) = result(futureLock, 5 seconds)
    cleanupLock = Some(lock)

    lock.isValid should be(right = true)
    
    val failFutureLock = channel.lock(0, 10, share = false)

    intercept[OverlappingFileLockException] {
      result(failFutureLock, 5 seconds)
    }

  }

  it should "write bytes to the file then read them back" in {
    implicit val exec: ForkJoinPool = new ForkJoinPool(3)
    val channel = RxAsynchronousFileChannel(Paths.get("/tmp/asyncfile"), CREATE, WRITE, READ)
    val bytes = new Array[Byte](32)
    Random.nextBytes(bytes)

    val (writeCount, _) = result(channel.write(ByteBuffer.wrap(bytes), 0), 5 seconds)

    writeCount should be (32)

    val readBuffer = ByteBuffer.allocate(32)
    val (readCount, _) = result(channel.read(readBuffer, 0), 5 seconds)

    readCount should be (32)
    readBuffer.array() should be (bytes)
  }
}
