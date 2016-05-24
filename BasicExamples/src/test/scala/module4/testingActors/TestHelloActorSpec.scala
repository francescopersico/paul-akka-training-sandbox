package module4.testingActors

import akka.actor._
import akka.testkit.TestActorRef
import suite.TestKitSpec

class TestHelloActorSpec extends TestKitSpec("test") {
  
  "A Hello actor" should "say Hello() when told SayHello()" in {
    val helloRef = TestActorRef[HelloActor]
    val helloActor = helloRef.underlyingActor

    helloRef ! SayHello()

    expectMsg(Hello())
  }
  
  it should "track that it received SayHello()" in {
    val helloRef = TestActorRef[HelloActor]
    val helloActor = helloRef.underlyingActor

    helloRef ! SayHello()

    assert(helloActor.receivedSayHello)
  }
}
