package com.doohh.akkaClustering.master;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.doohh.akkaClustering.deploy.AppConf;
import com.doohh.akkaClustering.util.AppNetInfo;
import com.doohh.akkaClustering.util.Node;

import akka.actor.ActorRef;
import akka.actor.Address;
import akka.actor.UntypedActor;
import akka.dispatch.OnComplete;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import akka.routing.RoundRobinGroup;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class Launcher extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	private Timeout timeout = new Timeout(Duration.create(5, "seconds"));
	private final ExecutionContext ec;
	private Hashtable<Address, Node> workers = new Hashtable<Address, Node>();
	private int nProcNodes = 0;
	private int nParamNode = 0;
	private int nSlaveNode = 0;
	private ActorRef procNode = null;
	private AppConf appConf = null;
	private AppNetInfo networkInfo = null;

	public Launcher() {
		ec = context().system().dispatcher();
	}

	@Override
	public void onReceive(Object message) throws Throwable {
		if (message instanceof AppConf) {
			this.appConf = (AppConf) message;
			log.info("receive appConf msg from mater: {}", appConf);
			Future<Object> f = Patterns.ask(getSender(), "getWorkers()", timeout);
			log.info("request worker list");

			f.onSuccess(new SaySuccess<Object>(), ec);
			f.onComplete(new SayComplete<Object>(), ec);
			f.onFailure(new SayFailure<Object>(), ec);
		}

		else if (message.equals("finish()")) {
			workers.get(getSender().path().address()).setProc(false);
			log.info("workers : {}", workers);
			getSender().tell("stopTask", getSelf());
			log.info("send msg(stopTask) to the TaskActor");
			networkInfo = null;
		}

		else {
			unhandled(message);

		}
	}

	public final class SaySuccess<T> extends OnSuccess<T> {
		@Override
		public final void onSuccess(T result) {
			log.info("Succeeded with " + result);
			workers = (Hashtable<Address, Node>) result;
			procNode = selectNodes(workers);
			appConf.setNetworkInfo(networkInfo);

			try {
				for (int i = 0; i < nProcNodes; i++) {
					// ex. role: param, roleIdx: 0
					if (i < nParamNode) {
						appConf.setRole("param");
						appConf.setRoleIdx(i);
					} else {
						appConf.setRole("slave");
						appConf.setRoleIdx(i - nParamNode);
					}
					log.info("send appConf to worker: {}", procNode);
					Future<Object> future = Patterns.ask(procNode, appConf, timeout);
					String rst = (String) Await.result(future, timeout.duration());
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				context().stop(procNode);
			}
		}
	}

	public final class SayFailure<T> extends OnFailure {
		@Override
		public final void onFailure(Throwable t) {
			log.info("Failed with " + t);
		}
	}

	public final class SayComplete<T> extends OnComplete<T> {
		@Override
		public final void onComplete(Throwable t, T result) {
			log.info("Completed.");
		}
	}

	public ActorRef selectNodes(Hashtable<Address, Node> workers) {
		List<String> routeePaths = new ArrayList<String>();
		networkInfo = new AppNetInfo();

		this.nParamNode = this.appConf.getNMaster();
		this.nSlaveNode = this.appConf.getNWorker();
		this.nProcNodes = this.nParamNode + this.nSlaveNode;

		log.info("select {} nodes for proc", this.nProcNodes);
		int i = 0;
		for (Node node : workers.values()) {
			if (node.isProc() == false) {
				String addr = node.getActorRef().path().toString();
				routeePaths.add(addr);
				node.setProc(true);
				log.info("set node state by true: {}", node);

				if (i < this.nParamNode) {
					networkInfo.getParamAddr().add(addr);
				} else {
					networkInfo.getSlaveAddr().add(addr);
				}

				if (routeePaths.size() == this.nProcNodes)
					break;
				i++;
			}
		}
		log.info("routeePaths : {}", routeePaths);
		ActorRef router = getContext().actorOf(new RoundRobinGroup(routeePaths).props(), "router");
		return router;
	}
}
