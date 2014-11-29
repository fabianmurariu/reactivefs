package com.bytes32.rxfs.store.actors.table

import akka.actor.{ActorLogging, FSM, Actor}
import akka.actor.Actor.Receive
import com.bytes32.rxfs.store.commands.{Get, Put}
import com.bytes32.rxfs.store.actors.table.MemTableActor.{Active, State}

class MemTableActor(data: Map[Array[Byte], Array[Byte]])
  extends FSM[State, Map[Array[Byte], Array[Byte]]]
  with ActorLogging{

  startWith(Active(), data)

  override def receive: Receive = {
//    case Put(id, key, value) =>
//    case Get(id, key) =>
    case msg@_ => log.info("Got message {}", msg)
  }
}

object MemTableActor {

  trait State
  case class Active() extends State
  case class Merging() extends State

}
