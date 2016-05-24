package module5.supervision

import akka.actor._
import akka.actor.SupervisorStrategy._
import scala.concurrent.duration._

object SupervisionDriverApp extends App {
  val system = ActorSystem("supervision")
  
  val level1 = system.actorOf(Props[Level1], "level1")
  
  level1 ! Message(1)
  level1 ! Message(2)
  level1 ! Message(3)
  Thread.sleep(1000L)  // why?
  level1 ! Message(4)
  
  Thread.sleep(2000L)
  
  system.terminate()
}

case class Message(index: Int)

class Level1 extends Actor {
  val level2 = context.actorOf(Props[Level2], "level2")
  
  override val supervisorStrategy =
    AllForOneStrategy(
         maxNrOfRetries = 10,
         withinTimeRange = 1 minute) {

     case _: Exception                => Restart
    }
  
  def receive = {
    case m: Message => level2 ! m
  }
}

class Level2 extends Actor {
  val level3 = context.actorOf(Props[Level3], "level3")
  
  override val supervisorStrategy =
    AllForOneStrategy(
         maxNrOfRetries = 10,
         withinTimeRange = 1 minute) {

     case _: ArithmeticException      => Resume
     case _: NullPointerException     => Restart
     case _: IllegalArgumentException => Stop
     case _: Exception                => Escalate
    }
  
  def receive = {
    case m: Message => level3 ! m
  }

  override def preStart(): Unit = {
    println("Level2 preStart")
  }
  
  override def postStop(): Unit = {
    println("Level2 postStop")
  }
  
  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    println("Level2 preRestart")
    super.preRestart(reason, message)
  }

  override def postRestart(reason: Throwable): Unit = {
    println("Level2 postRestart")
    super.postRestart(reason)
  }
}

class Level3 extends Actor {
  def receive = {
    case Message(index) =>
      index match {
        case 1 => throw new ArithmeticException("1")
        case 2 => throw new NullPointerException("2")
        case 3 => throw new Exception("3")
        case 4 => throw new IllegalArgumentException("4")
      }
  }

  override def preStart(): Unit = {
    println("Level3 preStart")
  }
  
  override def postStop(): Unit = {
    println("Level3 postStop")
  }
  
  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    println("Level3 preRestart")
    super.preRestart(reason, message)
  }

  override def postRestart(reason: Throwable): Unit = {
    println("Level3 postRestart")
    super.postRestart(reason)
  }
}
