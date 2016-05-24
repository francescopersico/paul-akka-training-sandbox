package module09.remoteA

import akka.actor._
import com.typesafe.config.ConfigFactory
import module09.remoteB._

object RemoteAStartup {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("RemoteA" , ConfigFactory.load("remoteA_application"))
    
    val a2 = system.actorOf(Props[RemoteA2], name = "a2OnRemoteA")

    val b1 = system.actorOf(Props[RemoteB1], name = "b1OnRemoteB")

    val a1 = system.actorOf(Props(classOf[RemoteA1], b1), name = "a1OnRemoteA")
  }
}
