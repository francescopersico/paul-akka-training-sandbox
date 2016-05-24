package module09.remoteA

import akka.actor._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class RemoteA1(b1: ActorRef) extends Actor {
  context.system.scheduler.scheduleOnce(5 seconds, self, Tick())
  
  def receive = {
    case t: Tick =>
      b1 ! "Ping from RemoteA1 after create of b1OnRemoteB"
      
    case m: String =>
      println(s"RECEIVED REPLY TO RemoteA1: $m")
  }
}

case class Tick()
