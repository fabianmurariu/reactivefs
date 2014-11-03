package com.bytes32.rxfs.core.op

import java.nio.ByteBuffer

import com.bytes32.rxfs.core.JavaConversions.Promise2CompletionHandlerJavaInteger

import scala.concurrent.Future

trait ReadOp {
  self: AsyncOp =>

  def read[A >: Null <: AnyRef](dst: ByteBuffer, position: Long, attachment: Option[A] = None): Future[(Int, A)] = {
    async[Integer, A, Int] {
      handler =>
        channel.read(dst, position, attachment.orNull, handler)
    }
  }

}
