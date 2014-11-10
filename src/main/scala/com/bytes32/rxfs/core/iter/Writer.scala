package com.bytes32.rxfs.core.iter

import com.bytes32.rxfs.core.io.WriteIO
import com.bytes32.rxfs.core.op.WriteOp

import scala.concurrent.Future

case class Writer(position: Long = 0, chunkSize: Int = 8 * 1024,
                  inner: Future[Option[(Array[Byte], Int)]] = Future.successful(None)) extends WriteIO {

  override def apply(op: WriteOp): Writer = {
    //TODO: for now this just writes empty bytes
    val writeFuture = op.write(new Array[Byte](chunkSize), position)
      .map {
      case (-1, _) => None
      case (`chunkSize`, bytes) => Some((bytes, chunkSize))
      case (readBytes, bytes) =>
        val dest = new Array[Byte](readBytes)
        Array.copy(bytes, 0, dest, 0, readBytes)
        Some((dest, readBytes))
    }(op.executor)
    Writer(position + chunkSize, chunkSize, writeFuture)
  }

}