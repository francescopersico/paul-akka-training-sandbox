package collaboration.views

import akka.actor._
import akka.persistence.query.{PersistenceQuery, EventEnvelope}
import akka.persistence.inmemory.query.journal.scaladsl.InMemoryReadJournal
import akka.stream.{Materializer, ActorMaterializer}
import akka.stream.scaladsl.Source
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.duration._

final case class QueryDiscussionView(discussionId: String)
final case class DiscussionViewResult(discussionId: String, view: Option[DiscussionView])
final case class DiscussionView(ownerId: String, category: String, subject: String, description: String)

object DiscussionsViews {
  private var view: Option[ActorRef] = None
  
  def apply(): ActorRef = view.getOrElse { throw new IllegalStateException("Must create first.") }
  
  def create(system: ActorSystem): Unit = {
    if (view.isEmpty) {
      view = Some(system.actorOf(Props[DiscussionsViews], "discussionsView"))
    }
  }
}

class DiscussionsViews extends Actor {
  var views = Map[String,DiscussionView]()
  var offset = 0L

  context.system.scheduler.schedule(5 seconds, 2 seconds, self, DiscussionsViewsProjectionTick())

  def receive = {
    case query: QueryDiscussionView =>
      sender ! DiscussionViewResult(query.discussionId, views.get(query.discussionId))
      
    case _: DiscussionsViewsProjectionTick =>
      projectEvents
  }

  def projectEvents: Unit = {
    implicit val materializer: Materializer = ActorMaterializer()(context.system)

    val journal =
      PersistenceQuery(context.system).readJournalFor[InMemoryReadJournal](InMemoryReadJournal.Identifier)

    journal.currentEventsByTag(tag = "discussion", offset).runForeach { envelope =>
  	  println(s"DiscussionsViews: $envelope")

  	  project(envelope.event)
  	  
  	  if (offset < envelope.offset)
        offset = envelope.offset
    }
  }
  
  def project(event: Any): Unit = {
    import collaboration.models._

    // events may arrive out of order so
    // operations must be idempotent
    event match {
      case e: DiscussionStarted =>
        val current = views.get(e.discussionId)
        if (current.isDefined) {
          val view = current.get
          views = views + (e.discussionId -> DiscussionView(e.ownerId, e.category, e.subject, e.description))
        } else
          views = views + (e.discussionId -> DiscussionView(e.ownerId, e.category, e.subject, e.description))
    }
  }
}

private final case class DiscussionsViewsProjectionTick()
