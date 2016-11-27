package example.barrier;

import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class SubActor extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	private ActorSelection master;
	private Timeout timeout = new Timeout(Duration.create(1, "minutes"));
	private int n;

	public SubActor(int n) {
		master = context().actorSelection("akka.tcp://deepDist@127.0.0.1:2551/user/mainActor");
		this.n = n;
	}

	@Override
	public void onReceive(Object message) throws Throwable {
		if (message.equals("run()")) {
			log.error("slave{}: before barrier()", n);
			Future<Object> future = Patterns.ask(master, "barrier()", timeout);
			String result = (String) Await.result(future, timeout.duration());
			log.error("slave{}: after barrier", n);
		}		
	}
}
