package module9

import akka.NotUsed
import akka.actor._
import akka.persistence.query.{PersistenceQuery, EventEnvelope}
import akka.persistence.inmemory.query.journal.scaladsl.InMemoryReadJournal
import akka.stream.{Materializer, ActorMaterializer}
import akka.stream.scaladsl.Source
import akka.testkit.TestActorRef
import java.util.UUID
import suite.TestKitSpec

class TestProduct extends TestKitSpec("test") {
  "Product" should "be created when receiving CreateProduct" in {
    val productId = UUID.randomUUID().toString
    val product = system.actorOf(Props(classOf[Product], productId), productId)
    
    product ! CreateProduct("Test", "This is a test.")
    
    val product2Id = UUID.randomUUID().toString
    val product2 = system.actorOf(Props(classOf[Product], product2Id), product2Id)
    
    product2 ! CreateProduct("Test2", "This is a second test.")
    
    implicit val materializer: Materializer = ActorMaterializer()(system)

    val readJournal =
      PersistenceQuery(system)
      	.readJournalFor[InMemoryReadJournal](
      	    InMemoryReadJournal.Identifier)

    Thread.sleep(1000L)
    
    var offset = 0L
    
    readJournal.currentEventsByTag(tag = "product", 0L).runForeach { envelope =>
  	  println(s"Event: $envelope")
  	  if (offset < envelope.offset)
        offset = envelope.offset
    }

    Thread.sleep(1000L)

    assert(offset == 2)
  }
}
