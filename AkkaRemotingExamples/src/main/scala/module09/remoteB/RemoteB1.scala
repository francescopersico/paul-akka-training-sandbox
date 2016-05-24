package module09.remoteB

import akka.actor._

class RemoteB1 extends Actor {
  def receive = {
    case m: String =>
      println(s"MESSAGE TO ${self.path}: $m")
      sender ! s"RemoteB1 received your message: $m"
      
      val a2 = context.actorSelection("akka.tcp://RemoteA@localhost:5051/user/a2OnRemoteA")
      
      println(s"SELECTION OF a2: ${a2}")
      
      a2 ! "Ping from RemoteB1 after selection of a1OnRemoteA"
  }
}
