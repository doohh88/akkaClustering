package com.doohh.akkaClustering.deploy;

import com.doohh.akkaClustering.dto.AppConf;
import com.doohh.akkaClustering.dto.Command;

import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.dispatch.OnComplete;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class Submit extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	private ActorSelection master;
	private Timeout timeout = new Timeout(Duration.create(10, "seconds"));
	private final ExecutionContext ec;

	public Submit() {
		ec = context().system().dispatcher();
	}

	@Override
	public void onReceive(Object message) throws Throwable {
		// send AppConf to master. After sending, shut down the Submit actor
		if (message instanceof Command) {
			Command cmd = (Command) message;
			log.info("received command: {}", cmd);
			if (cmd.getCommand().equals("submit()")) {
				AppConf appConf = (AppConf) cmd.getData();
				master = getContext().actorSelection(appConf.getMasterURL());
				Future<Object> ack = Patterns.ask(master, cmd, timeout);
				log.info("sended commnad to master for running: {}", cmd);

				ack.onSuccess(new OnSuccess<Object>() {
					@Override
					public void onSuccess(Object result) throws Throwable {
						log.info("Succeeded sending with: " + result);
					}

				}, ec);

				ack.onFailure(new OnFailure() {
					@Override
					public void onFailure(Throwable t) throws Throwable {
						log.info("Failed to send with: " + t);
					}
				}, ec);

				ack.onComplete(new OnComplete<Object>() {
					@Override
					public void onComplete(Throwable t, Object result) throws Throwable {
						log.info("Completed.");
						log.info("terminate submit process");
						getContext().system().terminate();
					}
				}, ec);
			}
		}

		else if (message instanceof String) {
			log.info("received msg = {}", (String) message);
		} else {
			log.info("received unhandled msg");
			unhandled(message);
		}
	}
}
