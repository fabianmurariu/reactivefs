package com.bytes32.rxfs.core.io

import com.bytes32.rxfs.core.FutureWrapper
import com.bytes32.rxfs.core.op.{WriteOp, AsyncOp, ReadOp}

trait AsyncIO extends FutureWrapper[Option[(Array[Byte], Int)]] {
  val position: Long
  val chunkSize: Int
}

trait WriteIO extends (WriteOp => AsyncIO) with AsyncIO
trait ReadIO extends (ReadOp => AsyncIO) with AsyncIO
