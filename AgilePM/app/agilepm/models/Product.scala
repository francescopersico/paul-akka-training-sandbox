package agilepm.models

import akka.actor._
import akka.persistence._
import collaboration.models.DiscussionRequested
import java.util.Date
import java.util.UUID
import play.api.libs.functional.syntax._
import play.api.libs.json._
import scala.collection.immutable.Seq

// commands
final case class AttachDiscussion(productId: String, discussionId: String)
final case class CreateProduct(name: String, description: String, requestDiscussion: Boolean)

// events
final case class ProductCreated(productId: String, name: String, description: String)
final case class DiscussionAttached(productId: String, discussionId: String)

object Product {
  def nextId: String = UUID.randomUUID.toString
}

class Product(productId: String) extends PersistentActor {

  override def persistenceId = productId
  
  var state: Option[ProductState] = None
  
  override def receiveCommand: Receive = {
    case command: CreateProduct =>
      var events = Seq[Any](ProductCreated(productId, command.name, command.description))
      
      if (command.requestDiscussion)
        events = events :+ DiscussionRequested(productId, "AgilePM", command.name, "Product Discussion")
      
      persistAll(events) { event =>
        event match {
          case event: ProductCreated => updateWith(event)
          case event: DiscussionRequested => updateWith(event)
        }
        
        // temporary, belongs in process manager; here it should be: sender ! events(0)
        sender ! CreateProductResult(productId, command.name, command.description, command.requestDiscussion)
      }
      
    case command: AttachDiscussion =>
      assert(command.productId == productId)
      assert(state.isDefined && state.get.discussionRequested)
      
      persist(DiscussionAttached(productId, command.discussionId)) { event =>
        updateWith(event)
      }
  }
  
  override def receiveRecover: Receive = {
    case event: ProductCreated => updateWith(event)
    case event: DiscussionRequested => updateWith(event)
    case event: DiscussionAttached => updateWith(event)
  }
  
  def updateWith(event: DiscussionAttached) {
    val current = state.get
    state = Some(ProductState(current.name, current.description, true, Some(event.discussionId)))
  }
  
  def updateWith(event: DiscussionRequested) {
    val current = state.get
    state = Some(ProductState(current.name, current.description, true, current.discussionId))
  }
  
  def updateWith(event: ProductCreated) {
    state = Some(ProductState(event.name, event.description, false, None))
  }
}

protected final case class ProductState(
    name: String,
    description: String,
    discussionRequested: Boolean,
    discussionId: Option[String])
