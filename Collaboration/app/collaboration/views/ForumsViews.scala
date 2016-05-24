package collaboration.views

import akka.actor._
import akka.persistence.query.{PersistenceQuery, EventEnvelope}
import akka.persistence.inmemory.query.journal.scaladsl.InMemoryReadJournal
import akka.stream.{Materializer, ActorMaterializer}
import akka.stream.scaladsl.Source
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.duration._

final case class QueryForumView(forumId: String)
final case class ForumViewResult(forumId: String, view: Option[ForumView])
final case class ForumView(ownerId: String, category: String, name: String, description: String)

object ForumsViews {
  private var view: Option[ActorRef] = None
  
  def apply(): ActorRef = view.getOrElse { throw new IllegalStateException("Must create first.") }
  
  def create(system: ActorSystem): Unit = {
    if (view.isEmpty) {
      view = Some(system.actorOf(Props[ForumsViews], "forumsView"))
    }
  }
}

class ForumsViews extends Actor {
  var views = Map[String,ForumView]()
  var offset = 0L

  context.system.scheduler.schedule(5 seconds, 2 seconds, self, ForumsViewsProjectionTick())

  def receive = {
    case query: QueryForumView =>
      sender ! ForumViewResult(query.forumId, views.get(query.forumId))
      
    case _: ForumsViewsProjectionTick =>
      projectEvents
  }

  def projectEvents: Unit = {
    implicit val materializer: Materializer = ActorMaterializer()(context.system)

    val journal =
      PersistenceQuery(context.system).readJournalFor[InMemoryReadJournal](InMemoryReadJournal.Identifier)

    journal.currentEventsByTag(tag = "forum", offset).runForeach { envelope =>
  	  println(s"ForumsViews: $envelope")

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
      case e: ForumStarted =>
        val current = views.get(e.forumId)
        if (current.isDefined) {
          val view = current.get
          views = views + (e.forumId -> ForumView(e.ownerId, e.category, e.topic, e.description))
        } else
          views = views + (e.forumId -> ForumView(e.ownerId, e.category, e.topic, e.description))
    }
  }
}

private final case class ForumsViewsProjectionTick()
