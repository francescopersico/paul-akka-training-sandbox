package module3.workers

import akka.actor._

object WorkersDriverApp extends App {
  val system = ActorSystem("MyWorkCoordination")
  
  val workCoordinator = system.actorOf(Props[SummingWorkCoordinator])
  
  val client = system.actorOf(Props(classOf[Client], workCoordinator))
  
  client ! Run()
  
  Thread.sleep(2000L)
  
  system.terminate()
}

case class Run()

class Client(workCoordinator: ActorRef) extends Actor {
  def receive = {
    case r: Run =>
      println("Client Run")
      workCoordinator ! StartValue(1)
      
    case ResultValue(value) =>
      println(s"Final result is: $value")
  }
}

case class StartValue(value: Int)
case class ResultValue(value: Int)
case class CalculationSeed(value: Int)
case class AddOneTo(value: Int)

class SummingWorkCoordinator extends Actor {
  val worker1 = context.actorOf(Props[SummingWorker1])
  val worker2 = context.actorOf(Props[SummingWorker2])
  var respondingWorkers = 0
  var sum = 0
  var requester: ActorRef = _
  
  def receive = {
    case StartValue(value) =>
      println("SummingWorkCoordinator StartValue")
      requester = sender
      worker1 ! CalculationSeed(value)
      worker2 ! CalculationSeed(value)
      
    case result: ResultValue =>
      println("SummingWorkCoordinator ResultValue")
      sum += result.value
      respondingWorkers += 1
      
      if (respondingWorkers == 2)
        requester ! ResultValue(sum)
  }
}

class SummingWorker1 extends Actor {
  val worker1A = context.actorOf(Props[Worker1A])
  val worker1B = context.actorOf(Props[Worker1B])
  var respondingWorkers = 0
  var sum = 0
  
  def receive = {
    case CalculationSeed(value) =>
      println("SummingWorker1 CalculationSeed")
      worker1A ! AddOneTo(value + 1)
      worker1B ! AddOneTo(value + 1)
      
    case result: ResultValue =>
      println("SummingWorker1 ResultValue")
      sum += result.value
      respondingWorkers += 1
      
      if (respondingWorkers == 2)
        context.parent ! ResultValue(sum)
  }
}

class Worker1A extends Actor {
  def receive = {
    case AddOneTo(value) =>
      println("Worker1A AddOneTo")
      sender ! ResultValue(value + 1)
  }
}

class Worker1B extends Actor {
  def receive = {
    case AddOneTo(value) =>
      println("Worker1B AddOneTo")
      sender ! ResultValue(value + 1)
  }
}

class SummingWorker2 extends Actor {
  val worker2A = context.actorOf(Props[Worker2A])
  
  def receive = {
    case CalculationSeed(value) =>
      println("SummingWorker2 CalculationSeed")
      worker2A ! AddOneTo(value + 1)
      
    case result: ResultValue =>
      println("SummingWorker2 ResultValue")
      println("Worker1B AddOneTo")
      context.parent ! result
  }
}

class Worker2A extends Actor {
  def receive = {
    case AddOneTo(value) =>
      println("Worker2A AddOneTo")
      sender ! ResultValue(value + 1)
  }
}
