package com.bytes32.rxfs.store.actors

import akka.actor.{Actor, ActorRef, Props}

trait ActorProvider {
  self: Actor =>

  /*
   * Don't be a psycho, always name your children
   */
  def makeActor(props: Props, name: String): ActorRef = context.actorOf(props, name)
}
