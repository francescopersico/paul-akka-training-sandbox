package agilepm.views

import akka.actor._
import akka.persistence.query.{PersistenceQuery, EventEnvelope}
import akka.persistence.inmemory.query.journal.scaladsl.InMemoryReadJournal
import akka.stream.{Materializer, ActorMaterializer}
import akka.stream.scaladsl.Source
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.duration._

final case class QueryProductView(productId: String)
final case class ProductViewResult(productId: String, view: Option[ProductView])
final case class ProductView(name: String, description: String, discussionRequested: Boolean, discussionId: String)

object ProductsViews {
  private var view: Option[ActorRef] = None
  
  def apply(): ActorRef = view.getOrElse { throw new IllegalStateException("Must create first.") }
  
  def create(system: ActorSystem): Unit = {
    if (view.isEmpty) {
      view = Some(system.actorOf(Props[ProductsViews], "productsView"))
    }
  }
}

class ProductsViews extends Actor {
  var views = Map[String,ProductView]()
  var offset = 0L

  context.system.scheduler.schedule(5 seconds, 2 seconds, self, ProductsViewsProjectionTick())

  def receive = {
    case query: QueryProductView =>
      sender ! ProductViewResult(query.productId, views.get(query.productId))
      
    case _: ProductsViewsProjectionTick =>
      projectEvents
  }

  def projectEvents: Unit = {
    implicit val materializer: Materializer = ActorMaterializer()(context.system)

    val journal =
      PersistenceQuery(context.system).readJournalFor[InMemoryReadJournal](InMemoryReadJournal.Identifier)

    journal.currentEventsByTag(tag = "product", offset).runForeach { envelope =>
  	  println(s"ProductsViews: $envelope")

  	  project(envelope.event)
  	  
  	  if (offset < envelope.offset)
        offset = envelope.offset
    }
  }
  
  def project(event: Any): Unit = {
    import agilepm.models._
    import collaboration.models._

    // events may arrive out of order so
    // operations must be idempotent
    event match {
      case e: ProductCreated =>
        val current = views.get(e.productId)
        if (current.isDefined) {
          val view = current.get
          views = views + (e.productId -> ProductView(e.name, e.description, view.discussionRequested, view.discussionId))
        } else
          views = views + (e.productId -> ProductView(e.name, e.description, false, ""))
          
      case e: DiscussionRequested =>
        val current = views.get(e.ownerId)
        if (current.isDefined) {
          val view = current.get
          views = views + (e.ownerId -> ProductView(view.name, view.description, true, view.discussionId))
        } else
          views = views + (e.ownerId -> ProductView("", "", true, ""))
          
      case e: DiscussionAttached =>
        val current = views.get(e.productId)
        if (current.isDefined) {
          val view = current.get
          views = views + (e.productId -> ProductView(view.name, view.description, view.discussionRequested, e.discussionId))
        } else
          views = views + (e.productId -> ProductView("", "", false, e.discussionId))
    }
  }
}

private final case class ProductsViewsProjectionTick()
