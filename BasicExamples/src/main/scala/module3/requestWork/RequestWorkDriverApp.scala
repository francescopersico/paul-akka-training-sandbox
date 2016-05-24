package module3.requestWork

import akka.actor._

object RequestWorkDriverApp extends App {
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
case class RequestWork()
case class WorkCompleted()

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
      sum += result.value
      respondingWorkers += 1
      
      println(s"SummingWorkCoordinator ResultValue ($respondingWorkers)")
      
      if (respondingWorkers == 2)
        requester ! ResultValue(sum)
  }
}

class SummingWorker1 extends Actor {
  val worker1A = context.actorOf(Props[Worker1A])
  val worker1B = context.actorOf(Props[Worker1B])
  var completedWorkers = 0
  var initialValue = 0
  var sum = 0
  
  def receive = {
    case CalculationSeed(value) =>
      println("SummingWorker1 CalculationSeed")
      initialValue = value + 1
      
    case result: ResultValue =>
      println("SummingWorker1 ResultValue")
      sum += result.value
      
    case rw: RequestWork =>
      sender ! AddOneTo(initialValue)
      
    case c: WorkCompleted =>
      completedWorkers += 1
      
      println(s"SummingWorker1 WorkCompleted($completedWorkers)")
      
      if (completedWorkers == 2)
        context.parent ! ResultValue(sum)
  }
}

class Worker1A extends Actor {
  var workCount = 0
  
  override def preStart() = {
    context.parent ! RequestWork()
  }
  
  def receive = {
    case AddOneTo(value) =>
      sender ! ResultValue(value + 1)
      
      workCount += 1
      
      println(s"Worker1A AddOneTo ($workCount)")

      if (workCount < 10)
        sender ! RequestWork()
      else
        sender ! WorkCompleted()
  }
}

class Worker1B extends Actor {
  var workCount = 0
  
  override def preStart() = {
    context.parent ! RequestWork()
  }
  
  def receive = {
    case AddOneTo(value) =>
      sender ! ResultValue(value + 1)
      
      workCount += 1
      
      println(s"Worker1B AddOneTo ($workCount)")
      
      if (workCount < 10)
        sender ! RequestWork()
      else
        sender ! WorkCompleted()
  }
}

class SummingWorker2 extends Actor {
  val worker2A = context.actorOf(Props[Worker2A])
  var completedWorkers = 0
  var initialValue = 0
  var sum = 0
  
  def receive = {
    case CalculationSeed(value) =>
      println("SummingWorker2 CalculationSeed")
      initialValue = value + 1
      
    case rw: RequestWork =>
      sender ! AddOneTo(initialValue)
      
    case c: WorkCompleted =>
      completedWorkers += 1
      
      println(s"SummingWorker2 WorkCompleted($completedWorkers)")
      
      if (completedWorkers >= 1)
        context.parent ! ResultValue(sum)

    case result: ResultValue =>
      println("SummingWorker2 ResultValue")
      sum += result.value
  }
}

class Worker2A extends Actor {
  var workCount = 0
  
  override def preStart() = {
    context.parent ! RequestWork()
  }
  
  def receive = {
    case AddOneTo(value) =>
      println(s"Worker2A AddOneTo ($workCount)")
      sender ! ResultValue(value + 1)
      
      workCount += 1
      
      if (workCount < 10)
        sender ! RequestWork()
      else
        sender ! WorkCompleted()
  }
}
