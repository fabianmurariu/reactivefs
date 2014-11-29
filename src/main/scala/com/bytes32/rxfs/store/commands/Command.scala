package com.bytes32.rxfs.store.commands

import akka.actor.ActorRef

sealed trait Command {
  val id: Long
}

sealed trait CommandResponse {
  val code: Long
}

case class Put(id: Long, key: Array[Byte], value: Array[Byte]) extends Command
case class Get(id: Long, key: Array[Byte]) extends Command
case class PutResponse(id: Long, code: Long) extends Command with CommandResponse
case class GetResponse(id: Long, value: Array[Byte]) extends Command
case class ErrorResponse(id: Long, code: Long, original: Command) extends Command with CommandResponse
