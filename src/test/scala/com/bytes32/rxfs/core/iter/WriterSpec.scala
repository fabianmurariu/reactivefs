package com.bytes32.rxfs.core.iter

import java.io.{BufferedInputStream, FileInputStream}

import com.bytes32.rxfs.core.HasTempFile
import org.scalatest.{Matchers, FlatSpec}
import play.api.libs.iteratee
import play.api.libs.iteratee.{Enumeratee, Iteratee, Enumerator}

import scala.concurrent.Await.result
import scala.concurrent.forkjoin.ForkJoinPool
import scala.concurrent.duration._
import scala.language.postfixOps

class WriterSpec extends FlatSpec with Matchers with HasTempFile{

  "writer" should "write chunks of bytes into a file" in {
    val input = Enumerator(Array[Byte](1, 2, 3), Array[Byte](4), Array[Byte](5, 6, 7), Array[Byte](8, 9))
    doWithTempFile("WriterSpec.toIteratee"){
      file =>
        val iterateeWriter = Writers.toIteratee(file)(new ForkJoinPool(3))
        val eventualWriter = input |>>> iterateeWriter
        result(result(eventualWriter, 5 seconds), 5 seconds) // should be(9)

        val stream = new BufferedInputStream(new FileInputStream(file))
        val bytes = new Array[Byte](9)
        stream.read(bytes)
        bytes should be(Array[Byte](1, 2, 3, 4, 5, 6, 7, 8, 9))
    }
  }

  it should "end early with the input" in {
    val input = Enumerator(Array[Byte](1, 2, 3), Array[Byte](4), Array[Byte](5, 6, 7), Array[Byte](8, 9))
    doWithTempFile("WriterSpec.toIteratee"){
      file =>
        val iterateeWriter = Writers.toIteratee(file)(new ForkJoinPool(3))
        val eventualWriter = input |>>> Enumeratee.take(2) &>> iterateeWriter
        result(result(eventualWriter, 5 seconds), 5 seconds) // should be(4)

        val stream = new BufferedInputStream(new FileInputStream(file))
        val bytes = new Array[Byte](9)
        stream.read(bytes)
        bytes should be(Array[Byte](1, 2, 3, 4, 0, 0, 0, 0, 0))
    }

  }

}
