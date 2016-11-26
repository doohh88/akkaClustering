package example.barrier;

import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import scala.concurrent.Await;
import scala.concurrent.Future;

public class SubActor extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	
	private ActorSelection master;
	
	public SubActor() {
		master = context().actorSelection("akka.tcp://deepDist@127.0.0.1:2551/user/mainActor");
	}
	
	@Override
	public void onReceive(Object message) throws Throwable {
		if(message.equals("run()")){
			log.info("before barrier");
			//master.tell("barrier()", getSender());
			Future<Object> future = Patterns.ask(master, "barrier()", timeout);
			Patterns.ask
			Integer result = (Integer) Await.result(future, timeout.duration());
		}
	}
}
