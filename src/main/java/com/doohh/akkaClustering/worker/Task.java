package com.doohh.akkaClustering.worker;

import com.doohh.akkaClustering.deploy.AppConf;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class Task extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	@Override
	public void onReceive(Object message) throws Throwable {
		if(message instanceof AppConf){
			AppConf appConf = (AppConf)message;
			log.info("i'm task : {}", appConf);
						
			getSender().tell("finish()", getSelf());
		}
	}
}
