package sample.infra

import akka.actor._
import akka.cluster.sharding._
import sample.models.{CommandEnvelope, Product}

object ProductShardManager {
  def region(system: ActorSystem): ActorRef =
    ClusterSharding(system).shardRegion(shardTypeName)
    
  def start(system: ActorSystem): ActorRef = {
    val region = ClusterSharding(system).start(
        typeName = shardTypeName,
        entityProps = Props[Product],
        settings = ClusterShardingSettings(system),
        extractEntityId = extractEntityId,
        extractShardId = extractShardId)

    region
  }
  
  private val shardTypeName = "Product"
  
  private val extractEntityId: ShardRegion.ExtractEntityId = {
    case CommandEnvelope(id, command) => (id.toString, command)
  }
 
  private val numberOfShards = 30
 
  private val extractShardId: ShardRegion.ExtractShardId = {
    case CommandEnvelope(id, _) => (Math.abs(id.hashCode) % numberOfShards).toString
  }
}