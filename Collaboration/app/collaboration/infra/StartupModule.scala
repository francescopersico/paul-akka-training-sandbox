package collaboration.infra

import akka.actor._
import akka.io.IO
import akka.io.Udp
import akka.util.ByteString
import collaboration.models._
import collaboration.views._
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

  ForumsViews.create(system)
  
  DiscussionsViews.create(system)
  
  TopicsFeeder.create(
      system,
      TopicFeederInfo(
          "ALL",
          Seq("forum", "discussion"),
          ws))
  
  TopicsReader.create(
      system,
      TopicReaderInfo(
          "ALL",
          system.actorOf(Props[TopicsReaderConsumer], "topicReaderConsumer-ALL"),
          ws))
  
  val properties = Map[String,String](
		  ("home" -> 		        s"http://${host}:${port}/collaboration"),
		  ("startForum" ->	    s"http://${host}:${port}/collaboration/forums"),
		  ("forum" ->		        s"http://${host}:${port}/collaboration/forums/:forumId"),
		  ("startDiscussion" -> s"http://${host}:${port}/collaboration/forums/:forumId/discussions"),
		  ("discussion" ->      s"http://${host}:${port}/collaboration/forums/:forumId/discussions/:discussionId")
      )
  
  val address = new InetSocketAddress(host, Integer.parseInt(broadcastPort))
  val collabServiceInfo = Json.toJson(properties).toString
  val heartbeat = system.actorOf(Props(classOf[Heartbeat], address, collabServiceInfo, ws), "heartbeat")
}

class Heartbeat(address: InetSocketAddress, collabServiceInfo: String, ws: WSClient) extends Actor {
  var registryInfo: Option[Map[String,String]] = None
  
  import scala.util.{Success, Failure}
  import context.system
  IO(Udp) ! Udp.Bind(self, address, List(Udp.SO.Broadcast(true)))
 
  context.system.scheduler.schedule(5 seconds, 5 seconds, self, collabServiceInfo)
  
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
      registerCollabService
      
    case Udp.Unbind  =>
      socket ! Udp.Unbind
    
    case Udp.Unbound =>
      context.stop(self)
  }
  
  private def acquireTopicsInfo: Unit = {
    val url = registryInfo.get("query").replace(":name", "Topics")
    
    val request: WSRequest = ws.url(url)
        
    request.get() onComplete {
      case Failure(t) => println("COLLAB FAILED GET TOPICS INFO: " + t.getMessage)
      case Success(result) =>
        println("COLLAB GOT TOPICS INFO: " + result.body)
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
  
  private def registerCollabService: Unit = {
    
    if (registryInfo.isDefined) {
      val url = registryInfo.get("register").replace(":name", "Collaboration")
      
      val request: WSRequest = ws.url(url).withHeaders("Content-Type" -> "application/json")
        
      request.put(collabServiceInfo) onComplete {
        case Failure(t) => println("COLLAB FAILED REGISTRATION: " + t.getMessage)
        case Success(result) => println("COLLAB REGISTERED INFO: " + result.body)
      }
    }
  }
}
