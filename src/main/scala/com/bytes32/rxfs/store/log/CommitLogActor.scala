package com.bytes32.rxfs.store.log

import akka.actor.{ActorLogging, Actor}
import com.bytes32.rxfs.store.commands.Command

class CommitLogActor(commitLog: List[Command]) extends Actor with ActorLogging{
  override def receive: Receive = {
    case msg@_ => {
      log.info("Received {} attempting to send to {}", msg, sender())
      sender ! msg
    }
  }
}
