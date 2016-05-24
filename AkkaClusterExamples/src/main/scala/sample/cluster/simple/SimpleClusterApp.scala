package sample.cluster.simple

import akka.actor.ActorSystem
import akka.actor.Props
import com.typesafe.config.ConfigFactory
import java.util.UUID
import sample.infra.ProductShardManager
import sample.models._

object SimpleClusterApp {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty)
      startup(Seq("2551", "2552", "0"))
    else
      startup(args)
  }

  def startup(ports: Seq[String]): Unit = {
    ports foreach { port =>
      // Override the configuration of the port
      val config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).
        withFallback(ConfigFactory.load())

      // Create an Akka system
      val system = ActorSystem("ClusterSystem", config)
      // Create an actor that handles cluster domain events
      system.actorOf(Props[SimpleClusterListener], name = "clusterListener")
      
      val region = ProductShardManager.start(system)
      
      Thread.sleep(3000L)
      
      val productId = UUID.randomUUID.toString
      
      region ! CommandEnvelope(productId, CreateProduct("Test-" + productId, "Description of " + productId))
      
      println(s"REGISTERED SHARD REGION ON SYSTEM: $system")
      println(s"  CREATED PRODUCT IN SHARD REGION: $productId")
    }
  }

}

