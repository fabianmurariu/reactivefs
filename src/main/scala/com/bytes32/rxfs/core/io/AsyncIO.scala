package com.bytes32.rxfs.core.io

import com.bytes32.rxfs.core.FutureWrapper
import com.bytes32.rxfs.core.op.{ReadOp, WriteOp}
import play.api.libs.iteratee.{Step, Iteratee}

import scala.concurrent.{ExecutionContext, Future}

trait AsyncIO {
  val position: Long
}

trait ReadIO extends (ReadOp => ReadIO) with AsyncIO with FutureWrapper[Option[(Array[Byte], Int)]]
trait WriteIO extends ((WriteOp, Array[Byte]) => WriteIO) with AsyncIO with FutureWrapper[Int]
