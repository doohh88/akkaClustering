package com.doohh.akkaClustering.worker;

import java.util.Hashtable;

import com.doohh.akkaClustering.deploy.AppConf;
import com.doohh.akkaClustering.master.Master;
import com.doohh.akkaClustering.util.Node;

import akka.actor.ActorRef;
import akka.actor.Address;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.Member;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import lombok.Getter;

@Getter
public class Worker extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	public static final String REGISTRATION_TO_WORKER = "Worker registrate the master";	
	private Cluster cluster = Cluster.get(getContext().system());
	private Hashtable<Address, Node> masters = new Hashtable<Address, Node>();
	private AppConf userAppConf;

	// subscribe to cluster changes, MemberUp
	@Override
	public void preStart() {
		cluster.subscribe(getSelf(), MemberUp.class);
	}

	// re-subscribe when restart
	@Override
	public void postStop() {
		cluster.unsubscribe(getSelf());
	}

	@Override
	public void onReceive(Object message) throws Throwable {
		if (message.equals(Master.REGISTRATION_TO_MASTER)) {
			log.info("received registration msg from the master");
			log.info("register the master at worker");
			masters.put(getSender().path().address(), new Node(getSender(), false));
			log.info("current masterTable: {}", masters);
		} else if (message instanceof MemberUp) {
			log.info("received MemberUp msg");
			MemberUp mUp = (MemberUp) message;
			log.info("send the msg to the master for handshaking");
			register(mUp.member());
		} else if (message instanceof MemberEvent) {
		}

		else if (message instanceof AppConf) {
			AppConf appConf = (AppConf) message;
			log.info("get appConf from master : {}", appConf);

			ActorRef task = context().actorOf(Props.create(Task.class), "task");
			log.info("generate task for proc");

			log.info("getSender: {}", getSender());
			task.tell(appConf, getSender());
		}

		else if (message instanceof String) {
			log.info("Get message = {}", (String) message);
		}

		else {
			log.info("receive unhandled msg");
			unhandled(message);
		}

	}

	void register(Member member) {
		if (member.hasRole("master")) {
			getContext().actorSelection(member.address() + "/user/master").tell(REGISTRATION_TO_WORKER, getSelf());
		}
	}
}
