package agilepm.infra

import akka.persistence.journal.{Tagged, WriteEventAdapter}

class ProductEventsTaggingAdapter extends WriteEventAdapter {
  override def manifest(event: Any): String = ""
  
  override def toJournal(event: Any): Any = {
    println(s"TAGGING product: $event")
    Tagged(event, Set("product"))
  }
}
