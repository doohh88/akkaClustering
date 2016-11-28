package com.doohh.akkaClustering.worker;

import java.util.ArrayList;

import com.doohh.akkaClustering.dto.AppConf;
import com.doohh.akkaClustering.dto.Command;
import com.doohh.akkaClustering.dto.DistInfo;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class Controller extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	private int curNWait = 0;
	private int barrierNum = 0;
	private ArrayList<ActorRef> returnList = new ArrayList<ActorRef>();

	@Override
	public void onReceive(Object message) throws Throwable {
		if (message instanceof Command) {
			Command cmd = (Command) message;

			if (cmd.getData() instanceof Integer) {
				this.barrierNum = (Integer) cmd.getData();
				log.info("barrier()");
				curNWait++;
				returnList.add(getSender());
				if (curNWait == this.barrierNum) {
					log.error("escape barrier()!!!!!!!!!!!!");
					for (ActorRef tmp : returnList) {
						tmp.tell("ack", getSelf());
					}
					curNWait = 0;
					returnList.clear();
				}
			}

			if (cmd.getCommand().equals("finishApp()")) {
				log.info("finishApp()");
				ActorSelection master = context().actorSelection(((AppConf) cmd.getData()).getMasterURL());
				master.tell(cmd, getSelf());
			}
		}
	}

	public static void barrier(DistInfo distInfo, String type) {
		Command cmd = new Command().setCommand("barrier()");
		if (type.equals("all"))
			cmd.setData(distInfo.getRouterInfo().getNNodes());
		else if (type.equals("param"))
			cmd.setData(distInfo.getRouterInfo().getNParamServer());
		else if (type.equals("slave"))
			cmd.setData(distInfo.getRouterInfo().getNProcServer());

		try {
			Timeout timeout = new Timeout(Duration.create(5, "minutes"));
			Future<Object> future = Patterns.ask(distInfo.getController(), cmd, timeout);
			String result = (String) Await.result(future, timeout.duration());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getCurrentMethodName() {
		StackTraceElement[] stacks = new Throwable().getStackTrace();
		StackTraceElement currentStack = stacks[1];
		return "" + currentStack.getClassName() + "." + currentStack.getMethodName();
	}
}