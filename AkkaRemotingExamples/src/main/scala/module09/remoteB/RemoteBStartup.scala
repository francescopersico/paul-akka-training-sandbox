package module09.remoteB

import akka.actor._
import com.typesafe.config.ConfigFactory
import java.io.File

object RemoteBStartup {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("RemoteB" , ConfigFactory.load("remoteB_application"))
  }
}
