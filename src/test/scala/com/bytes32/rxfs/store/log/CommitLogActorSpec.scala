package com.bytes32.rxfs.store.log

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.bytes32.rxfs.store.commands.{Command, Put}
import com.typesafe.config.ConfigFactory
import org.scalatest._

import scala.concurrent.duration._
import scala.language.postfixOps

class CommitLogActorSpec
  extends TestKit(ActorSystem("CommitLogSpec", ConfigFactory.parseString(CommitLogActorSpec.config)))
  with WordSpecLike with Matchers with BeforeAndAfterAll with ImplicitSender{

  override def afterAll() = {
    shutdown()
  }

  "A CommitLogActor" should {
    "response with number of bytes appended to disk" in {
      val cLog = system.actorOf(Props(classOf[CommitLogActor], List[Command]()), name = "CommitLogActor_in_Test")
      within(500 millis) {
        val put = Put(1L, Array[Byte](1, 2, 3), Array[Byte](3, 2, 1))
        cLog ! put
        expectMsg(put)
      }
    }
  }
}

object CommitLogActorSpec {
  lazy val config =
    """
      |akka {
      | loglevel = "INFO"
      |}
    """.stripMargin
}
