package com.bytes32.rxfs.store.actors

import akka.actor.{Actor, ActorRef, Props}
import akka.testkit.TestProbe

trait TestActorProvider extends ActorProvider {
  self: Actor =>

  var probe: TestProbe = _

  override def makeActor(props: Props, name: String): ActorRef = {
    probe = new TestProbe(context.system)
    probe.ref
  }

}
