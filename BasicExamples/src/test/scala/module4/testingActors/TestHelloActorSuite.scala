package module4.testingActors

import akka.actor._
import akka.pattern.ask
import akka.testkit.TestActorRef
import akka.util.Timeout
import org.scalatest.FunSuite
import scala.concurrent.Await
import scala.concurrent.duration._

class TestHelloActorSuite extends FunSuite {
  implicit val system = ActorSystem("test")
  
  test("a HelloActor says Hello() when told to SayHello()") {
    val helloRef = TestActorRef[HelloActor]

    implicit val timeout = Timeout(5 seconds)
    val future = helloRef ? SayHello()

    val answer = Await.result(future, timeout.duration).asInstanceOf[Hello]
    
    assert(answer == Hello())
  }

  test("a HelloActor track that it received SayHello()") {
    val helloRef = TestActorRef[HelloActor]
    val helloActor = helloRef.underlyingActor

    helloRef ! SayHello()

    assert(helloActor.receivedSayHello)
  }
}
