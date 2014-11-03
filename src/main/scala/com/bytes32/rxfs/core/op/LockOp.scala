package com.bytes32.rxfs.core.op

import java.nio.channels.FileLock

import com.bytes32.rxfs.core.JavaConversions.Promise2CompletionHandler

import scala.concurrent.Future

trait LockOp {
  self: AsyncOp =>

  def lock[A >: Null <: AnyRef](position: Long, size: Long, share: Boolean, attachment: Option[A] = None): Future[(FileLock, A)] = {
    async[FileLock, A, FileLock] {
      handler =>
        channel.lock(position, size, share, attachment.orNull, handler)
    }
  }

}
