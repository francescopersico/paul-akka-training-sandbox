package agilepm.infra

import agilepm.models._
import agilepm.views._
import akka.actor._
import akka.io.IO
import akka.io.Udp
import akka.util.ByteString
import com.google.inject.AbstractModule
import common.infra._
import java.net.InetSocketAddress
import javax.inject._
import play.api._
import play.api.http.HttpEntity
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.libs.ws._
import scala.concurrent.duration._
import java.nio.charset.Charset

class StartupModule extends AbstractModule {
  override def configure() = {
    bind(classOf[Startup]).to(classOf[StartupRunner]).asEagerSingleton
  }
}

trait Startup {
  val zero = 0
}

@Singleton
class StartupRunner @Inject() (system: ActorSystem, ws: WSClient) extends Startup {
  val host = System.getProperty("http.host")
  val port = System.getProperty("http.port")
  val broadcastPort = System.getProperty("broadcast.port", "9998")

  ProductsViews.create(system)
  
  TopicsFeeder.create(
      system,
      TopicFeederInfo(
          "ALL",
          Seq("product"),
          ws))
  
  TopicsReader.create(
      system,
      TopicReaderInfo(
          "ALL",
          system.actorOf(Props[TopicsReaderConsumer], "topicReaderConsumer-ALL"),
          ws))
  
  val properties = Map[String,String](
		  ("home" -> 		s"http://${host}:${port}/agilepm"),
		  ("newProduct" ->	s"http://${host}:${port}/agilepm/products"),
		  ("product" ->		s"http://${host}:${port}/agilepm/products/:id")
      )
  
  val address = new InetSocketAddress(host, Integer.parseInt(broadcastPort))
  val agilepmServiceInfo = Json.toJson(properties).toString
  val heartbeat = system.actorOf(Props(classOf[Heartbeat], address, agilepmServiceInfo, ws), "heartbeat")
}

class Heartbeat(address: InetSocketAddress, agilepmServiceInfo: String, ws: WSClient) extends Actor {
  var registryInfo: Option[Map[String,String]] = None
  
  import scala.util.{Success, Failure}
  import context.system
  IO(Udp) ! Udp.Bind(self, address, List(Udp.SO.Broadcast(true)))
 
  context.system.scheduler.schedule(5 seconds, 5 seconds, self, agilepmServiceInfo)
  
  def receive = {
    case Udp.Bound(local) =>
      context.become(ready(sender))
  }

  def ready(socket: ActorRef): Receive = {
    case Udp.Received(data, remote) =>
      val json = data.decodeString("UTF-8")
      registryInfo = Some(Json.parse(json).as[Map[String,String]])
      acquireTopicsInfo
      
    case info: String =>
      registerAgilePMService
      
    case Udp.Unbind  =>
      socket ! Udp.Unbind
    
    case Udp.Unbound =>
      context.stop(self)
  }
  
  private def acquireTopicsInfo: Unit = {
    val url = registryInfo.get("query").replace(":name", "Topics")
    
    val request: WSRequest = ws.url(url)
        
    request.get() onComplete {
      case Failure(t) => println("AGILEPM FAILED GET TOPICS INFO: " + t.getMessage)
      case Success(result) =>
        println("AGILEPM GOT TOPICS INFO: " + result.body)
        val info = Json.parse(result.body.toString).as[Map[String,String]]
        val appenderLocation = info.get("append")
        if (appenderLocation.isDefined)
          TopicsFeeder() ! TopicAppenderLocation(appenderLocation.get)
        val currentLogLocation = info.get("currentLog")
        val messageLocation = info.get("message")
        if (currentLogLocation.isDefined && messageLocation.isDefined)
          TopicsReader() ! TopicReaderLocation(currentLogLocation.get, messageLocation.get)
    }
  }
  
  private def registerAgilePMService: Unit = {
    
    if (registryInfo.isDefined) {
      val url = registryInfo.get("register").replace(":name", "AgilePM")
      
      val request: WSRequest = ws.url(url).withHeaders("Content-Type" -> "application/json")
        
      request.put(agilepmServiceInfo) onComplete {
        case Failure(t) => println("AGILEPM FAILED REGISTRATION: " + t.getMessage)
        case Success(result) => println("AGILEPM REGISTERED INFO: " + result.body)
      }
    }
  }
}
