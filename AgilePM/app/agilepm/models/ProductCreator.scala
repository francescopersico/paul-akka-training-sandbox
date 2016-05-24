package agilepm.models

import akka.actor.Actor

case class CreateProductResult(productId: String, name: String, description: String, discussionRequested: Boolean)

class ProductCreator extends Actor {
  def receive = {
    case _ =>
  }
}
