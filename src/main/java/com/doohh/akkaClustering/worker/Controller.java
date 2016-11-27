package com.doohh.akkaClustering.worker;

import java.util.ArrayList;
import java.util.Hashtable;

import com.doohh.akkaClustering.dto.Barrier;
import com.doohh.akkaClustering.dto.Command;
import com.doohh.akkaClustering.dto.DistInfo;

import akka.actor.ActorRef;
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
	private Hashtable<String, Barrier> barrierTable = new Hashtable<String, Barrier>();

	@Override
	public void onReceive(Object message) throws Throwable {
		if (message instanceof Command) {
			Command cmd = (Command) message;
			if (cmd.getCommand().equals("barrier()")) {
				if (cmd.getData() instanceof Barrier) {
					Barrier barrier = (Barrier) cmd.getData();
					if (barrierTable.contains(barrier) == false)
						barrierTable.put(barrier.getMethodName(), barrier);

					barrier = barrierTable.get(barrier.getMethodName());
					barrier.getReturnList().add(getSender());
					if (barrier.count() == true) {
						returnList = barrier.getReturnList();
						for (ActorRef ar : returnList) {
							ar.tell("ack", getSelf());
						}
						barrierTable.remove(barrier.getMethodName());
					}
				}

			}

			if (cmd.getData() instanceof Integer) {
				this.barrierNum = (Integer) cmd.getData();
				log.info("i'm main: barrier() from {}", getSender().path().address());
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

	// 아마 안 쓸듯..
	public static void barrier(DistInfo distInfo, String type, String methodName) {
		Command cmd = new Command().setCommand("barrier()");
		if (type.equals("all"))
			cmd.setData(new Barrier(distInfo.getRouterInfo().getNNodes(), methodName));
		else if (type.equals("param"))
			cmd.setData(new Barrier(distInfo.getRouterInfo().getNParamServer(), methodName));
		else if (type.equals("slave"))
			cmd.setData(new Barrier(distInfo.getRouterInfo().getNProcServer(), methodName));

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
