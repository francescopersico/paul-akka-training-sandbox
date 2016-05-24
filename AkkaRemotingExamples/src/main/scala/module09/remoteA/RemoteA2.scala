package module09.remoteA

import akka.actor._

class RemoteA2 extends Actor {
  def receive = {
    case m: Any =>
      println(s"RECEIVED MESSAGE TO RemoteA2: $m")
  }
}
