package com.doohh.akkaClustering.worker;

import java.util.ArrayList;

import com.doohh.akkaClustering.dto.AppConf;
import com.doohh.akkaClustering.dto.BarrierInfo;
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
	private int max = 0;
	private int sum;
	private int cnt = 0;
	private int recvIter;
	private boolean hasNext;
	private boolean escapeFlag = false;
	private ArrayList<ActorRef> returnList = new ArrayList<ActorRef>();

	@Override
	public void onReceive(Object message) throws Throwable {
		if (message instanceof Command) {
			Command cmd = (Command) message;

			if (cmd.getData() instanceof BarrierInfo) {
				BarrierInfo barrierInfo = (BarrierInfo) cmd.getData();
				this.barrierNum = barrierInfo.getBarrierNum();
				hasNext = barrierInfo.isHasNext();
				if (hasNext == false)
					escapeFlag = true;

				if (this.barrierNum == 0)
					log.error("clear barrier()!!!!");
				log.info("barrier()");
				curNWait++;
				returnList.add(getSender());
				if (curNWait == this.barrierNum) {
					log.error("escape barrier()!!!!!!!!!!!!");
					boolean send = false;
					if (escapeFlag == true) {
						send = true;
					}
					for (ActorRef tmp : returnList) {
						tmp.tell(send, getSelf());
					}
					escapeFlag = false;
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
		barrier(distInfo, type, true);
	}

	public static boolean barrier(DistInfo distInfo, String type, boolean hasNext) {
		Command cmd = new Command().setCommand("barrier()");
		BarrierInfo barrierInfo;
		if (type.equals("all"))
			barrierInfo = new BarrierInfo(distInfo.getRouterInfo().getNNodes(), hasNext);
		else if (type.equals("param"))
			barrierInfo = new BarrierInfo(distInfo.getRouterInfo().getNParamServer(), hasNext);
		else if (type.equals("slave"))
			barrierInfo = new BarrierInfo(distInfo.getRouterInfo().getNProcServer(), hasNext);
		else if (type.equals("clear"))
			barrierInfo = new BarrierInfo(0, hasNext);
		else
			barrierInfo = new BarrierInfo(0, hasNext);
		cmd.setData(barrierInfo);

		boolean rst = false;
		try {
			Timeout timeout = new Timeout(Duration.create(10, "minutes"));
			Future<Object> future = Patterns.ask(distInfo.getController(), cmd, timeout);
			rst = (boolean) Await.result(future, timeout.duration());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rst;
	}

	public static String getCurrentMethodName() {
		StackTraceElement[] stacks = new Throwable().getStackTrace();
		StackTraceElement currentStack = stacks[1];
		return "" + currentStack.getClassName() + "." + currentStack.getMethodName();
	}
}

// public static boolean barrier(DistInfo distInfo, String type, int iteration)
// {
// Command cmd = new Command().setCommand("barrier()");
// BarrierInfo barrierInfo;
// if (type.equals("all"))
// barrierInfo = new BarrierInfo(distInfo, distInfo.getRouterInfo().getNNodes(),
// iteration);
// else if (type.equals("param"))
// barrierInfo = new BarrierInfo(distInfo,
// distInfo.getRouterInfo().getNParamServer(), iteration);
// else if (type.equals("slave"))
// barrierInfo = new BarrierInfo(distInfo,
// distInfo.getRouterInfo().getNProcServer(), iteration);
// else if (type.equals("clear"))
// barrierInfo = new BarrierInfo(distInfo, 0, iteration);
// else
// barrierInfo = new BarrierInfo(distInfo, 0, 0);
// cmd.setData(barrierInfo);
//
// boolean rst = false;
// try {
// Timeout timeout = new Timeout(Duration.create(10, "minutes"));
// Future<Object> future = Patterns.ask(distInfo.getController(), cmd, timeout);
// rst = (boolean) Await.result(future, timeout.duration());
//
// } catch (Exception e) {
// e.printStackTrace();
// }
// return rst;
// }

// if (type.equals("all"))
// cmd.setData(distInfo.getRouterInfo().getNNodes());
// else if (type.equals("param"))
// cmd.setData(distInfo.getRouterInfo().getNParamServer());
// else if (type.equals("slave"))
// cmd.setData(distInfo.getRouterInfo().getNProcServer());
// else if (type.equals("clear"))
// cmd.setData(0);

// System.out.println("rst: " + rst);
// for(int i = 0 ;i < rst; i++){
// System.out.println("rebarrier " + i);
// barrier(distInfo, "slave");
//// future = Patterns.ask(distInfo.getController(), cmd, timeout);
//// Await.result(future, timeout.duration());
// }