package com.bytes32.rxfs.store.actors.log

import akka.actor.{Props, ActorLogging, FSM}
import com.bytes32.rxfs.core.iter.Writer
import com.bytes32.rxfs.core.op.{LockOp, WriteOp}
import com.bytes32.rxfs.store.actors.ActorProvider
import com.bytes32.rxfs.store.commands.{ErrorResponse, Command}
import com.bytes32.rxfs.store.actors.log.TransactionLogActor.{Active, TransactionLogData, State}
import com.bytes32.rxfs.store.actors.table.MemTableActor

import scala.concurrent.ExecutionContext.Implicits.global

import scala.pickling._
import scala.pickling.fastbinary._
import scala.util.{Success, Failure}

class TransactionLogActor(state: State, data: TransactionLogData)
  extends FSM[State, TransactionLogData]
  with ActorLogging
  with ActorProvider{

  startWith(state, data)

  val memTable = makeActor(Props(new MemTableActor(Map[Array[Byte], Array[Byte]]())), "memTable")

  override def receive: Receive = {
    case cmd: Command =>
      val senderRef = sender()
      val actorData = data(cmd)
      actorData.writer.onComplete {
        case Success(_) => memTable ! cmd
        case Failure(ex) => senderRef ! ErrorResponse(cmd.id, 0, cmd)
      }
      stay using actorData
//    case msg@_ =>
//      log.info("Received unknown {} attempting to send to {}", msg, sender())
//      sender ! msg
  }
}

object TransactionLogActor {

  /* state*/
  trait State

  case class Active() extends State

  case class Purging() extends State

  /* Data */
  case class TransactionLogData(writeOp: WriteOp with LockOp, writer: Writer)
    extends (Command => TransactionLogData) {

    override def apply(command: Command): TransactionLogData = {
      val bytes = command.pickle
      TransactionLogData(writeOp, writer(writeOp, bytes.value))
    }

  }

}
