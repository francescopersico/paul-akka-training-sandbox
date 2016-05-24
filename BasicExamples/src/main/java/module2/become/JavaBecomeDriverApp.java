package module2.become;

import akka.actor.*;
import akka.japi.Procedure;

public class JavaBecomeDriverApp {
  public static void main(String[] args) {
    ActorSystem system = ActorSystem.create("Moods");
    
    ActorRef mood = system.actorOf(Props.create(MoodActor.class), "mood");
    
    for (int idx = 1; idx <= 20; idx++) {
  	  if (idx % 2 == 0)
  	    mood.tell(new Laugh(idx), ActorRef.noSender());
  	  else
  		mood.tell(new Smile(idx), ActorRef.noSender());
  	}
    
    mood.tell(new Laugh(0), ActorRef.noSender());
    mood.tell(new Smile(0), ActorRef.noSender());
	
    try {
      Thread.sleep(2000L);
    } catch (InterruptedException e) {
      // ignore
    }
	
    system.terminate();
  }

  public static final class Laugh {
    public final int idx;

    public Laugh(int idx) {
      this.idx = idx;
    }
  }
  
  public static final class Smile {
    public final int idx;

    public Smile(int idx) {
      this.idx = idx;
    }
  }

  public static class MoodActor extends UntypedActor {
    @Override
    public void preStart() {
      getContext().become(smiler);
    }
    
    @Override
    public void onReceive(Object msg) {
      System.out.println("Oops!");
    }
    
    private Procedure<Object> laugher = new Procedure<Object>() {
      @Override
      public void apply(Object msg) {
        if (msg instanceof Laugh) {
          System.out.println("Ha, ha, ha! " + ((Laugh) msg).idx);
          getContext().become(smiler);
        }
      }
    };

    private Procedure<Object> smiler = new Procedure<Object>() {
      @Override
      public void apply(Object msg) {
        if (msg instanceof Smile) {
          System.out.println("Cheese! " + ((Smile) msg).idx);
          getContext().become(laugher);
        }
      }
    };
  }
}
