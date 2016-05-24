package collaboration.models

import akka.actor._
import akka.persistence._

// commands
final case class StartForum(ownerId: String, category: String, topic: String, description: String)

// events
final case class ForumStarted(forumId: String, ownerId: String, category: String, topic: String, description: String)

class Forum(forumId: String) extends PersistentActor {
  
  override def persistenceId = forumId
  
  var state: Option[ForumState] = None
  
  override def receiveCommand: Receive = {
    case command: StartForum =>
      val started = ForumStarted(forumId, command.ownerId, command.category, command.topic, command.description)
      persist(started) { event =>
        updateWith(event)
        sender ! event
      }
  }
  
  override def receiveRecover: Receive = {
    case event: ForumStarted => updateWith(event)
  }
  
  def updateWith(event: ForumStarted): Unit = {
    state = Some(ForumState(event.topic, event.description))
  }
}

protected final case class ForumState(topic: String, description: String)
