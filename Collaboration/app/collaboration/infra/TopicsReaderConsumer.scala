package collaboration.infra

import akka.actor._
import collaboration.models._
import common.infra._
import common.tools._

class TopicsReaderConsumer extends Actor {
  def receive = {
    case consume: TopicReaderConsumerMessage =>
      val reader = new ObjectReader(consume.message)
      
      val messageType = reader.stringValue("messageType")
      
      messageType match {
        case "collaboration.models.DiscussionRequested" =>
          println(s"############# TopicsReaderConsumer: RECEIVED MESSAGE: $messageType")

          val ownerId = reader.stringValue("message.ownerId")
          val category = reader.stringValue("message.category")
          val forumTopic = reader.stringValue("message.forumTopic")
          val discussionSubject = reader.stringValue("message.discussionSubject")
          
          ForumDiscussionStarter(context.system) ! StartForumDiscussion(ownerId, category, forumTopic, discussionSubject)
          
        case _ =>
          println(s"TopicsReaderConsumer: RECEIVED MESSAGE: $messageType")
      }
      
    case result: StartForumDiscussionResult =>
      println(s"############# TopicsReaderConsumer: RECEIVED COMPLETION: $result")
  }
}
