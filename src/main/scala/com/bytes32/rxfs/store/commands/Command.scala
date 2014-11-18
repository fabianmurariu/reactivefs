package com.bytes32.rxfs.store.commands

sealed trait Command {
  val id: Long
}

case class Put(id: Long, key: Array[Byte], value: Array[Byte]) extends Command

case class Get(id: Long, key: Array[Byte]) extends Command

case class PutResponse(id: Long) extends Command

case class GetResponse(id: Long, value: Array[Byte]) extends Command
