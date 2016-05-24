package module5.supervision;

import static akka.actor.SupervisorStrategy.escalate;
import static akka.actor.SupervisorStrategy.restart;
import static akka.actor.SupervisorStrategy.resume;
import static akka.actor.SupervisorStrategy.stop;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.AllForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.actor.SupervisorStrategy.Directive;
import akka.actor.UntypedActor;
import akka.japi.Function;

public class JavaSupervisionDriverApp {
  public static void main(String[] args) {
    ActorSystem system = ActorSystem.create("Supervision");
    
    ActorRef level1 = system.actorOf(Props.create(Level1.class), "level1");
        
    level1.tell(new Message(1), ActorRef.noSender());
    level1.tell(new Message(2), ActorRef.noSender());
    level1.tell(new Message(3), ActorRef.noSender());
    sleep(1000L);  // why?
    level1.tell(new Message(4), ActorRef.noSender());
  
    sleep(2000L);
    
    system.terminate();
  }
  
  public static void sleep(long duration) {
    try {
      Thread.sleep(duration);
    } catch (InterruptedException e) {
      // ignore
    }
  }
  
  public static final class Message {
    public final int index;
    public Message(int index) { this.index = index; }
  }
  
  public static class Level1 extends UntypedActor {
    private ActorRef level2;
    
    private static SupervisorStrategy strategy =
        new AllForOneStrategy(10, Duration.create("1 minute"),
          new Function<Throwable, Directive>() {
            @Override
            public Directive apply(Throwable t) {
              return restart(); // all throwables
            }
          });
       
    @Override
    public SupervisorStrategy supervisorStrategy() {
      return strategy;
    }
    
    @Override
    public void preStart() {
      level2 = getContext().actorOf(Props.create(Level2.class), "level2");
    }
    
    @Override
    public void onReceive(Object msg) {
      if (msg instanceof Message) {
        level2.tell(msg, getSelf());
      }
    }
  }
  
  public static class Level2 extends UntypedActor {
    private ActorRef level3;
    
    private static SupervisorStrategy strategy =
        new AllForOneStrategy(10, Duration.create("1 minute"),
          new Function<Throwable, Directive>() {
            @Override
            public Directive apply(Throwable t) {
              if (t instanceof ArithmeticException) {
                return resume();
              } else if (t instanceof NullPointerException) {
                return restart();
              } else if (t instanceof IllegalArgumentException) {
                return stop();
              } else {
                return escalate();
              }
            }
          });
     
    @Override
    public SupervisorStrategy supervisorStrategy() {
      return strategy;
    }
      
    @Override
    public void preStart() {
      System.out.println("Level2 preStart");
      level3 = getContext().actorOf(Props.create(Level3.class), "level3");
    }

    @Override
    public void postStop() {
      System.out.println("Level2 postStop");
    }
      
    @Override
    public void preRestart(Throwable reason, scala.Option<Object> message) throws Exception {
      System.out.println("Level2 preRestart");
      super.preRestart(reason, message);
    }

    @Override
    public void postRestart(Throwable reason) throws Exception {
      System.out.println("Level2 postRestart");
      super.postRestart(reason);
    }
    
    @Override
    public void onReceive(Object msg) {
      if (msg instanceof Message) {
        level3.tell(msg, getSelf());
      }
    }
  }

  public static class Level3 extends UntypedActor {
    @Override
    public void onReceive(Object msg) {
      if (msg instanceof Message) {
        switch (((Message) msg).index) {
        case 1:
          throw new ArithmeticException("1");
        case 2:
          throw new NullPointerException("2");
        case 3:
          throw new RuntimeException("3");
        case 4:
          throw new IllegalArgumentException("4");
        }
      }
    }

    @Override
    public void preStart() {
      System.out.println("Level3 preStart");
    }
    
    @Override
    public void postStop() {
      System.out.println("Level3 postStop");
    }
    
    @Override
    public void preRestart(Throwable reason, scala.Option<Object> message) throws Exception {
      System.out.println("Level3 preRestart");
      super.preRestart(reason, message);
    }

    @Override
    public void postRestart(Throwable reason) throws Exception {
      System.out.println("Level3 postRestart");
      super.postRestart(reason);
    }
  }
}
