package com.bytes32.rxfs.core.iter

import java.io.{File, FileOutputStream}

import com.bytes32.rxfs.core.JavaConversions.forkJoinPool2ExecutionContext
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.iteratee.{Enumerator, Iteratee}

import scala.concurrent.Await.result
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.concurrent.forkjoin.ForkJoinPool
import scala.language.postfixOps

class ReaderSpec extends FlatSpec with Matchers {

  "Reader" should "read the bytes from a file as they are written" in {
    val tempFile = File.createTempFile("ReaderSpec", "toEnumerator")
    try {
      val out = new FileOutputStream(tempFile)
      val bytes = "this is my test how is yours?".getBytes
      out.write(bytes)
      out.close()
      //TODO: need a better option instead of running around with 2 executors
      implicit val forkJoinPool = new ForkJoinPool(3)
      implicit val ex: ExecutionContext = forkJoinPool
      val enumerator: Enumerator[Array[Byte]] = Readers.toEnumerator(tempFile.getAbsolutePath, 5)
      val eventualArray = enumerator |>>> Iteratee.fold(Array[Byte]())((chunk, result) => chunk ++ result)

      result(eventualArray, 5 seconds) should be(bytes)
    } finally {
      tempFile.delete()
    }
  }

}
