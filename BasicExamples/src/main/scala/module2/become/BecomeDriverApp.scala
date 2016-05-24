package module2.become

import akka.actor._

object BecomeDriverApp extends App {
	val system = ActorSystem("Moods")
	
	val mood = system.actorOf(Props[MoodActor])
	
	for (idx <- 1 to 20) {
	  if (idx % 2 == 0)
	    mood ! Laugh(idx)
	  else
	    mood ! Smile(idx)
	}
	
	mood ! Laugh(0)
	mood ! Smile(0)
	
	Thread.sleep(2000L)
	
	system.terminate()
}

case class Laugh(idx: Int)
case class Smile(idx: Int)

class MoodActor extends Actor {
  context.become(smiler)

  def receive = {
    case _ => println("Oops!")
  }
  
  def laugher: Receive = {
    case Laugh(idx) =>
      println(s"Ha, ha, ha! $idx")
      context.become(smiler)
  }

  def smiler: Receive = {
    case Smile(idx) =>
      println(s"Cheese! $idx")
      context.become(laugher)
  }
}
