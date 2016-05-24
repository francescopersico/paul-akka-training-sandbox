package module4.testingActors;

import org.junit.*;

import akka.actor.*;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;

import static module4.testingActors.JavaHelloActor.Hello;
import static module4.testingActors.JavaHelloActor.SayHello;
import static module4.testingActors.JavaHelloActor.HelloActor;

public class TestJavaHelloActor {

  static ActorSystem system;
  
  @BeforeClass
  public static void setup() {
    system = ActorSystem.create();
  }
  
  @AfterClass
  public static void teardown() {
    JavaTestKit.shutdownActorSystem(system);
    system = null;
  }
  
  @Test
  public void testHello() {
    new JavaTestKit(system) {{
      Props props = Props.create(HelloActor.class);
      ActorRef hello = system.actorOf(props);
      hello.tell(new SayHello(), getRef());
      expectMsgEquals(duration("1 second"), new Hello());
    }};
  }
  
  @Test
  public void testHelloActorInternals() {
    new JavaTestKit(system) {{
      Props props = Props.create(HelloActor.class);
      TestActorRef<HelloActor> ref = TestActorRef.create(system, props, "hello");
      HelloActor actor = ref.underlyingActor();
      Assert.assertFalse(actor.receivedSayHello);
      ref.tell(new SayHello(), getRef());
      expectMsgEquals(duration("1 second"), new Hello());
      Assert.assertTrue(actor.receivedSayHello);
    }};
  }
}
