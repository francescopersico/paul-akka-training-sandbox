package module9

import akka.actor._
import akka.persistence._
import java.util.Date

// commands
final case class CreateProduct(name: String, description: String)

// events
trait DomainEvent { val occurredOn = new Date() }
final case class ProductCreated(productId: String, name: String, description: String)

class Product(productId: String) extends PersistentActor {
  
  override def persistenceId = productId
  
  var state: Option[ProductState] = None
  
  override def receiveCommand: Receive = {
    case command: CreateProduct =>
      persist(ProductCreated(productId, command.name, command.description)) { event =>
        updateWith(event)
      }
  }
  
  override def receiveRecover: Receive = {
    case event: ProductCreated =>
      updateWith(event)
  }
  
  def updateWith(event: ProductCreated) {
    state = Some(ProductState(event.name, event.description))
  }
}

protected final case class ProductState(name: String, description: String)

import akka.persistence.journal.{Tagged, WriteEventAdapter}

class ProductEventsTaggingAdapter extends WriteEventAdapter {
  override def manifest(event: Any): String = ""
  
  override def toJournal(event: Any): Any = {
    println(s"TAGGING product: $event")
    Tagged(event, Set("product"))
  }
}
