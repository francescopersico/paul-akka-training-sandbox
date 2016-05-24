package module3.workers;

import akka.actor.*;

public class JavaWorkersDriverApp {
  public static void main(String[] args) {
    ActorSystem system = ActorSystem.create("MyWorkCoordination");
    
    ActorRef workCoordinator = system.actorOf(Props.create(SummingWorkCoordinator.class));
    
    ActorRef client = system.actorOf(Props.create(Client.class, workCoordinator));

    client.tell(new Run(), ActorRef.noSender());
  
    try {
      Thread.sleep(2000L);
    } catch (InterruptedException e) {
      // ignore
    }

    system.terminate();
  }

  public static final class Run { }
  
  public static class Client extends UntypedActor {
    private final ActorRef workCoordinator;
    
    public Client(ActorRef workCoordinator) {
      this.workCoordinator = workCoordinator;
    }
    
    public void onReceive(Object msg) {
      if (msg instanceof Run) {
        System.out.println("Client Run");
        workCoordinator.tell(new StartValue(1), getSelf());
      } else if (msg instanceof ResultValue) {
        System.out.println("Final result is: " + ((ResultValue) msg).value);
      }
    }
  }
  
  public static final class StartValue {
    public final int value;
    public StartValue(int value) { this.value = value; }
  }

  public static final class ResultValue {
    public final int value;
    public ResultValue(int value) { this.value = value; }
  }

  public static final class CalculationSeed {
    public final int value;
    public CalculationSeed(int value) { this.value = value; }
  }

  public static final class AddOneTo {
    public final int value;
    public AddOneTo(int value) { this.value = value; }
  }
  
  public static class SummingWorkCoordinator extends UntypedActor {
    private ActorRef worker1;
    private ActorRef worker2;
    private int respondingWorkers;
    private int sum;
    private ActorRef requester;
    
    @Override
    public void preStart() {
      worker1 = getContext().actorOf(Props.create(SummingWorker1.class));
      worker2 = getContext().actorOf(Props.create(SummingWorker2.class));
    }
    
    public void onReceive(Object msg) {
      if (msg instanceof StartValue) {
        System.out.println("SummingWorkCoordinator StartValue");
        requester = getSender();
        worker1.tell(new CalculationSeed(((StartValue) msg).value), getSelf());
        worker2.tell(new CalculationSeed(((StartValue) msg).value), getSelf());
      } else if (msg instanceof ResultValue) {
        System.out.println("SummingWorkCoordinator ResultValue");
        sum += ((ResultValue) msg).value;
        respondingWorkers++;
        
        if (respondingWorkers == 2)
          requester.tell(new ResultValue(sum), getSelf());
      }
    }
  }

  public static class SummingWorker1 extends UntypedActor {
    private ActorRef worker1A;
    private ActorRef worker1B;
    private int respondingWorkers;
    private int sum;
    
    @Override
    public void preStart() {
      worker1A = getContext().actorOf(Props.create(Worker1A.class));
      worker1B = getContext().actorOf(Props.create(Worker1B.class));
    }
    
    @Override
    public void onReceive(Object msg) {
      if (msg instanceof CalculationSeed) {
        CalculationSeed seed = (CalculationSeed) msg;
        System.out.println("SummingWorker1 CalculationSeed");
        worker1A.tell(new AddOneTo(seed.value + 1), getSelf());
        worker1B.tell(new AddOneTo(seed.value + 1), getSelf());
      } else if (msg instanceof ResultValue) {
        ResultValue result = (ResultValue) msg;
        System.out.println("SummingWorker1 ResultValue");
        sum += result.value;
        respondingWorkers += 1;
        
        if (respondingWorkers == 2)
          getContext().parent().tell(new ResultValue(sum), getSelf());
      }
    }
  }

  public static class Worker1A extends UntypedActor {
    @Override
    public void onReceive(Object msg) {
      if (msg instanceof AddOneTo) {
        System.out.println("Worker1A AddOneTo");
        getSender().tell(new ResultValue(((AddOneTo) msg).value + 1), getSelf());
      }
    }
  }

  public static class Worker1B extends UntypedActor {
    @Override
    public void onReceive(Object msg) {
      if (msg instanceof AddOneTo) {
        System.out.println("Worker1B AddOneTo");
        getSender().tell(new ResultValue(((AddOneTo) msg).value + 1), getSelf());
      }
    }
  }

  public static class SummingWorker2 extends UntypedActor {
    private ActorRef worker2A;
    
    @Override
    public void preStart() {
      worker2A = getContext().actorOf(Props.create(Worker2A.class));
    }
    
    @Override
    public void onReceive(Object msg) {
      if (msg instanceof CalculationSeed) {
        System.out.println("SummingWorker2 CalculationSeed");
        worker2A.tell(new AddOneTo(((CalculationSeed) msg).value + 1), getSelf());
      } else if (msg instanceof ResultValue) {
        System.out.println("SummingWorker2 ResultValue");
        System.out.println("Worker1B AddOneTo");
        getContext().parent().tell(msg, getSelf());
      }
    }
  }

  public static class Worker2A extends UntypedActor {
    @Override
    public void onReceive(Object msg) {
      if (msg instanceof AddOneTo) {
        System.out.println("Worker2A AddOneTo");
        getSender().tell(new ResultValue(((AddOneTo) msg).value + 1), getSelf());
      }
    }
  }
}
