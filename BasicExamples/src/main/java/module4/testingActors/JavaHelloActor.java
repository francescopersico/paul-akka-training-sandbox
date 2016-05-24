package module4.testingActors;

import akka.actor.*;

public class JavaHelloActor {

  public static final class SayHello {}
  
  public static final class Hello {
    @Override
    public boolean equals(Object other) {
      return (other instanceof Hello);
    }
  }

  public static class HelloActor extends UntypedActor {
    public boolean receivedSayHello = false;

    @Override
    public void onReceive(Object msg) {
      if (msg instanceof SayHello) {
        receivedSayHello = true;
        
        getSender().tell(new Hello(), getSelf());
      }
    }
  }
}
