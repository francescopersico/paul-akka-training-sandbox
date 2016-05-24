package collaboration.models

import akka.actor._
import akka.persistence._

// commands
final case class StartDiscussion(ownerId: String, category: String, subject: String, description: String)

// events
final case class DiscussionStarted(discussionId: String, ownerId: String, category: String, subject: String, description: String)

class Discussion(discussionId: String) extends PersistentActor {
  
  override def persistenceId = discussionId
  
  var state: Option[DiscussionState] = None
  
  override def receiveCommand: Receive = {
    case command: StartDiscussion =>
      val started = DiscussionStarted(discussionId, command.ownerId, command.category, command.subject, command.description)
      persist(started) { event =>
        updateWith(event)
        sender ! event
      }
  }
  
  override def receiveRecover: Receive = {
    case event: DiscussionStarted => updateWith(event)
  }
  
  def updateWith(event: DiscussionStarted): Unit = {
    state = Some(DiscussionState(event.subject, event.description))
  }
}

protected final case class DiscussionState(subject: String, description: String)
