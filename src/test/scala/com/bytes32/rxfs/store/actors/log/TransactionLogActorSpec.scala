package com.bytes32.rxfs.store.actors.log

import java.nio.file.Paths
import java.nio.file.StandardOpenOption.WRITE

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestProbe, ImplicitSender, TestActorRef, TestKit}
import com.bytes32.rxfs.core.HasTempFile
import com.bytes32.rxfs.core.io.RxAsynchronousFileChannel
import com.bytes32.rxfs.core.iter.Writer
import com.bytes32.rxfs.store.actors.TestActorProvider
import com.bytes32.rxfs.store.commands.Put
import com.bytes32.rxfs.store.actors.log.TransactionLogActor.{Active, TransactionLogData}
import com.typesafe.config.ConfigFactory
import org.scalatest._

import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.forkjoin.ForkJoinPool
import scala.language.postfixOps

class TransactionLogActorSpec
  extends TestKit(ActorSystem("TransactionLogActorSystem", ConfigFactory.parseString(TransactionLogActorSpec.config)))
  with WordSpecLike with Matchers with BeforeAndAfterAll with ImplicitSender with HasTempFile {

  implicit val exec = new ForkJoinPool(5)

  override def afterAll() = {
    shutdown()
  }

  "A TransactionLogActor" should {
    "forward the command to memTable actor on success" in {
      doWithTempFile("TransactionLogActor") {
        file =>
          /* prepare the async file channel */
          val channel = RxAsynchronousFileChannel(Paths.get(file.getAbsolutePath), WRITE)

          /* actor under test */
          val cLog = TestActorRef[TransactionLogActor with TestActorProvider] (
            Props(new TransactionLogActor(Active(), TransactionLogData(channel, Writer(0))) with TestActorProvider))

          /* data */
          val cmds = List(
            Put(1L, Array[Byte](1, 1, 1), Array[Byte](3, 2, 1)),
            Put(2L, Array[Byte](2, 2, 2), Array[Byte](4, 5, 6)),
            Put(3L, Array[Byte](3, 3, 3), Array[Byte](7, 8, 9))
          )
          /* send data */
          for (cmd <- cmds) {
            cLog ! cmd
          }
          within(500 millis) {
            for (cmd <- cmds) {
              cLog.underlyingActor.probe.expectMsg(cmd)
            }
          }
      }
    }
  }
}

object TransactionLogActorSpec {
  lazy val config =
    """
      |akka {
      | loglevel = "INFO"
      |}
    """.stripMargin
}
