package sample.models

import akka.actor._
import akka.persistence._
import java.util.Date

// commands
final case class CommandEnvelope(id: String, command: Any)

final case class CreateProduct(name: String, description: String)

// events
final case class ProductCreated(productId: String, name: String, description: String)

class Product extends PersistentActor {
  
  private val productId = self.path.name // name must be aggregate unique id
  
  override def persistenceId = productId
  
  var state: Option[ProductState] = None
  
  override def receiveCommand: Receive = {
    case command: CreateProduct =>
      println(s"********* PRODUCT RECEIVED: $command")
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
