package collaboration.models

import akka.actor._
import java.util.UUID

final case class StartForumDiscussion(
    ownerId: String,
    category: String,
    forumTopic: String,
    discussionSubject: String)

final case class StartForumDiscussionResult(
    forumId: String,
    discussionId: String,
    ownerId: String,
    category: String,
    forumTopic: String,
    discussionSubject: String)

object ForumDiscussionStarter {
  def apply(system: ActorSystem): ActorRef = {
    system.actorOf(Props[ForumDiscussionStarter], "ForumDiscussionStarter-" + UUID.randomUUID.toString)
  }
}
    
class ForumDiscussionStarter extends Actor {
  def receive = {
    case start: StartForumDiscussion =>
      val forumId = UUID.randomUUID.toString
      val forum = context.system.actorOf(Props(classOf[Forum], forumId), forumId)
      forum ! StartForum(start.ownerId, start.category, start.forumTopic, s"Forum: ${start.forumTopic}")
      context.become(forumStartedListener(StartForumDiscussionInfo(sender, start, forumId)))
  }
  
  def forumStartedListener(info: StartForumDiscussionInfo): Receive = {
    case event: ForumStarted =>
      val discussionId = UUID.randomUUID.toString
      val discussion = context.system.actorOf(Props(classOf[Discussion], discussionId), discussionId)
      discussion ! StartDiscussion(info.start.ownerId, info.start.category, info.start.discussionSubject, s"Discussion: ${info.start.discussionSubject}")
      context.become(discussionStartedListener(info.withDiscussionId(discussionId)))
  }
  
  def discussionStartedListener(info: StartForumDiscussionInfo): Receive = {
    case event: DiscussionStarted =>
      info.sender ! StartForumDiscussionResult(info.forumId, info.discussionId, info.start.ownerId, info.start.category, info.start.forumTopic, info.start.discussionSubject)
  }
}

private final case class StartForumDiscussionInfo(
    sender: ActorRef,
    start: StartForumDiscussion,
    forumId: String,
    discussionId: String) {
  
  def this(sender: ActorRef, start: StartForumDiscussion, forumId: String) =
    this(sender, start, forumId, "")

  def withDiscussionId(discussionId: String): StartForumDiscussionInfo =
    StartForumDiscussionInfo(this.sender, this.start, this.forumId, discussionId)
}

private object StartForumDiscussionInfo {
  def apply(sender: ActorRef, start: StartForumDiscussion, forumId: String): StartForumDiscussionInfo =
    new StartForumDiscussionInfo(sender, start, forumId)
}
