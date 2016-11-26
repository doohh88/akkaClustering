package example.barrier;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class MainActor extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	
	private int nWorker;
	private int curNWait; 
	
	public MainActor(int nWorker) {
		this.nWorker = nWorker;
		this.curNWait = 0;
	}
	
	@Override
	public void onReceive(Object message) throws Throwable {
		if(message.equals("barrier()")){
			log.info("i'm main: barrier()");
			curNWait++;
			if(curNWait == nWorker){
				curNWait = 0;	
			}
		}
	}
}
