package agilepm.infra

import agilepm.models._
import akka.actor._
import common.infra._
import common.tools._

class TopicsReaderConsumer extends Actor {
  def receive = {
    case consume: TopicReaderConsumerMessage =>
      val reader = new ObjectReader(consume.message)
      val messageType = reader.stringValue("messageType")
      
      messageType match {
        case "collaboration.models.DiscussionStarted" =>
          if (reader.stringValue("message.category") == "AgilePM") {
            println("################ DISCUSSION STARTED")
            println("################ " + consume.message)
            
            val productId = reader.stringValue("message.ownerId")
            val discussionId = reader.stringValue("message.discussionId")
            
            context.system.actorSelection(s"/user/${productId}").tell(AttachDiscussion(productId, discussionId), self)
          }
          
        case _ =>
          // not consumed here
      }
  }
}
