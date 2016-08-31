package com.doohh.akkaClustering.deploy;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.dispatch.OnComplete;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class Submit extends UntypedActor {
	Logger log = LoggerFactory.getLogger(Submit.class);

    final String path = "akka.tcp://deepDist@127.0.0.1:2551/user/master";
	private ActorSelection master;
	private Timeout timeout = new Timeout(Duration.create(5, "seconds"));
	private final ExecutionContext ec;

	
    public Submit() {
    	master = getContext().actorSelection(path);
		ec = context().system().dispatcher();
	}
    
	@Override
	public void onReceive(Object message) throws Throwable {
			if(message instanceof UserAppConf){
				UserAppConf userAppConf = (UserAppConf)message;
				System.out.println(userAppConf.getPackagePath());
				System.out.println(userAppConf.getMainClass());
				Future<Object> future = Patterns.ask(master, userAppConf, timeout);
				
	            future.onSuccess(new SaySuccess<Object>(), ec);
	            future.onComplete(new SayComplete<Object>(), ec);
	            future.onFailure(new SayFailure<Object>(), ec);
				
				//응답이 오면 shutdown
			} else {
				unhandled(message);
			}
	}
	
	public final class SaySuccess<T> extends OnSuccess<T> {
	    @Override public final void onSuccess(T result) {
	    	log.info("Succeeded with " + result);
		}
	}
	
	public final class SayFailure<T> extends OnFailure {
	    @Override public final void onFailure(Throwable t) {
	    	log.info("Failed with " + t);
		}
	}
	
	public final class SayComplete<T> extends OnComplete<T> {
	    @Override public final void onComplete(Throwable t, T result) {
	    	log.info("Completed.");
			getContext().system().shutdown();
		}
	}
}
