package com.bytes32.rxfs.core.op

import java.nio.ByteBuffer

import com.bytes32.rxfs.core.JavaConversions.Promise2CompletionHandlerJavaInteger

import scala.concurrent.Future

trait WriteOp extends AsyncOp{

  def write[A >: Null <: AnyRef](src: ByteBuffer, position: Long, attachment: Option[A] = None): Future[(Int, A)] = {
    async[Integer, A, Int] {
      handler =>
        channel.write(src, position, attachment.orNull, handler)
    }
  }

  def write(dst: Array[Byte], position: Long): Future[(Int, _)] = {
    write(ByteBuffer.wrap(dst), position)
  }
}
