package suite

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{FlatSpecLike, BeforeAndAfterAll}
import org.scalatest.matchers.MustMatchers

abstract class TestKitSpec(name: String)
    extends TestKit(ActorSystem(name))
	with FlatSpecLike
    with MustMatchers
    with BeforeAndAfterAll
    with ImplicitSender {

  override def afterAll() {
    system.shutdown()
  }
}
