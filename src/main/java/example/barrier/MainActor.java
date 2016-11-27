package example.barrier;

import java.util.ArrayList;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class MainActor extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	
	private int nWorker;
	private int curNWait; 
	ArrayList<ActorRef> list = new ArrayList<ActorRef>();
	
	public MainActor(int nWorker) {
		this.nWorker = nWorker;
		this.curNWait = 0;
	}
	
	@Override
	public void onReceive(Object message) throws Throwable {
		if(message.equals("barrier()")){
			log.info("i'm main: barrier() from {}", getSender().path().address());
			curNWait++;
			list.add(getSender());
			if(curNWait == nWorker){
				System.out.println("escape barrier()");
				curNWait = 0;
				for(ActorRef tmp : list){
					tmp.tell("ack", getSelf());
				}
			}
		}
	}
}


/*String path = getSender().path().address() + "/user/subActor";
System.out.println(path);
ActorSelection as = context().actorSelection(getSender().path().address() + "/user/subActor");
list.add(as);
if(curNWait == nWorker){
	System.out.println("escape barrier()");
	curNWait = 0;
	//list.get(0).pathString()
	for(ActorSelection tmp : list){
		System.out.println(tmp.pathString());
		tmp.tell("ack", getSelf());
	}
}*/