package module4.testingActors

import akka.actor._

case class QueryTotal()
case class Clear()
case class Enter(value: Int)
case class Add(value: Int)
case class Subtract(value: Int)

class Calculator extends Actor {
  var total = 0
  
  def receive = {
    case _: QueryTotal => sender ! total
    case _: Clear => total = 0
    case Enter(value) => total = value
    case Add(value) => total += value
    case Subtract(value) => total -= value
  }
}
