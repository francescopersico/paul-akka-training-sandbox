package module4.testingActors

import akka.actor._

case class SayHello()
case class Hello()

class HelloActor extends Actor {
  var receivedSayHello = false
  
  def receive = {
    case _: SayHello =>
      receivedSayHello = true
      
      sender ! Hello()
  }
}
