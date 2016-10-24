package com.doohh.akkaClustering.master;

import java.util.Hashtable;

import com.doohh.akkaClustering.dto.AppConf;
import com.doohh.akkaClustering.dto.Command;
import com.doohh.akkaClustering.dto.RouterInfo;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.dispatch.OnComplete;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import akka.routing.Broadcast;
import akka.util.Timeout;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class Launcher extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	private Timeout timeout = new Timeout(Duration.create(5, "seconds"));
	private final ExecutionContext ec;
	private ActorSelection resourceMngr = null;
	private Hashtable<ActorRef, Integer> routerTable = null;

	public Launcher() {
		ec = context().system().dispatcher();
	}

	@Override
	public void preStart() throws Exception {
		resourceMngr = getContext().actorSelection("/user/master/resourceMngr");
		routerTable = new Hashtable<ActorRef, Integer>();
	}

	@Override
	public void onReceive(Object message) throws Throwable {
		if (message instanceof Command) {
			Command cmd = (Command) message;
			if (cmd.getCommand().equals("submit()")) {
				AppConf appConf = (AppConf) cmd.getData();
				log.info("received command msg from mater: {}", cmd);

				log.info("requested router for deployment");
				Future<Object> routerInfo = Patterns.ask(resourceMngr, new Command("getResource()", appConf), timeout);
				routerInfo.onSuccess(new OnSuccess<Object>() {
					@Override
					public void onSuccess(Object result) throws Throwable {
						log.info("Succeeded with " + routerInfo);
						RouterInfo routerInfo = (RouterInfo) result;
						try {
							routerTable.put(routerInfo.getRouter(), 0);
							deployAppConfwithRole(appConf, routerInfo);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}, ec);
				routerInfo.onFailure(new OnFailure() {
					@Override
					public void onFailure(Throwable t) throws Throwable {
						log.info("Failed to send with: " + t);
					}
				}, ec);

				routerInfo.onComplete(new OnComplete<Object>() {
					@Override
					public void onComplete(Throwable t, Object result) throws Throwable {
						log.info("Completed.");
					}
				}, ec);
			}

			if (cmd.getCommand().equals("finishApp()")) {
				log.info("finishApp");
				RouterInfo routerInfo = (RouterInfo) cmd.getData();
				ActorRef router = routerInfo.getRouter();
				routerTable.put(router, routerTable.get(router) + 1);
				if (routerTable.get(router) == routerInfo.getNNodes()) {
					// stop all task;
					router.tell(new Broadcast(new Command().setCommand("stopTask()").setData(null)), getSelf());
					context().stop(router);
				}
			}

		}

		else if (message instanceof String) {
			log.info("received msg = {}", (String) message);
		} else {
			log.info("received unhandled msg");
			unhandled(message);
		}
	}

	private void deployAppConfwithRole(AppConf appConf, RouterInfo routerInfo) throws Throwable {
		ActorRef procNodes = routerInfo.getRouter();

		// send appConf to router
		int nProcNodes = appConf.getNMaster() + appConf.getNWorker();
		int nParamNodes = appConf.getNMaster();
		Command cmd = new Command().setCommand("submit()");

		appConf.setRouterInfo(routerInfo); // set routerInfo
		for (int i = 0; i < nProcNodes; i++) {
			// set Role. role: param, roleIdx: 0
			if (i < nParamNodes) {
				appConf.setRole("param");
				appConf.setRoleIdx(i);
			} else {
				appConf.setRole("slave");
				appConf.setRoleIdx(i - nParamNodes);
			}

			log.info("send appConf to worker: {}", procNodes);
			Future<Object> future = Patterns.ask(procNodes, cmd.setData(appConf), timeout);
			future.onSuccess(new OnSuccess<Object>() {
				@Override
				public void onSuccess(Object result) throws Throwable {
					String ack = (String) result;
					log.info("get ack: {}", ack);
				}
			}, ec);
			future.onFailure(new OnFailure() {
				@Override
				public void onFailure(Throwable t) throws Throwable {
					log.info("Failed to send with: " + t);
				}
			}, ec);
			future.onComplete(new OnComplete<Object>() {
				@Override
				public void onComplete(Throwable t, Object result) throws Throwable {
					log.info("Completed.");
				}
			}, ec);
		}
	}
}

// routerTable.put(router, 0);
// System.out.println(routerTable);
// Future<Object> futuer = Patterns.ask(router, new Broadcast(new
// Command().setCommand("stopTask()").setData(null)), timeout);
// futuer.onSuccess(new OnSuccess<Object>() {
// @Override
// public void onSuccess(Object result) throws Throwable {
// log.info("Succeeded with " + result);
//
// System.out.println("hello" + getSender());
// resourceMngr.tell(new
// Command().setCommand("setProcFalse()").setData(getSender().path().address()),
// getSelf());
// }
// }, ec);
// futuer.onFailure(new OnFailure() {
// @Override
// public void onFailure(Throwable t) throws Throwable {
// log.info("Failed to send with: " + t);
// }
// }, ec);
//
// futuer.onComplete(new OnComplete<Object>() {
// @Override
// public void onComplete(Throwable t, Object result) throws Throwable {
// log.info("Completed.");
// routerTable.put(router, routerTable.get(router) + 1);
// System.out.println(routerTable);
// if (routerTable.get(router) == routerInfo.getNNodes()) {
// System.out.println("remove router");
// routerTable.remove(router);
// context().stop(router);
// }
// }
// }, ec);