package com.doohh.akkaClustering.worker;

import java.util.HashMap;

import com.doohh.akkaClustering.deploy.Launcher;
import com.doohh.akkaClustering.deploy.UserAppConf;
import com.doohh.akkaClustering.master.Master;

import akka.actor.ActorRef;
import akka.actor.Address;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.Member;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import lombok.Getter;

@Getter
public class Worker extends UntypedActor {
	public static final String WORKER_REGISTRATION = "WorkerRegistration";
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	Cluster cluster = Cluster.get(getContext().system());
	HashMap<Address, ActorRef> masters = new HashMap<Address, ActorRef>();
	UserAppConf userAppConf;
	
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
		if (message.equals(Master.MASTER_REGISTRATION)) {
			getContext().watch(getSender());
			masters.put(getSender().path().address(), getSender());
			log.info("master list = {}", masters.toString());
		} else if (message instanceof MemberUp) {
			MemberUp mUp = (MemberUp) message;
			register(mUp.member());
		} else if (message instanceof String) {
			log.info("Get message = {}", (String) message);
		} else {
			unhandled(message);
		}

	}

	void register(Member member) {
		// log.info("register() -> {} : {}",member.roles(), member.address());
		if (member.hasRole("master")) {
			getContext().actorSelection(member.address() + "/user/master").tell(WORKER_REGISTRATION, getSelf());
			// log.info("master(member.address()) : {}", member.address());
		}
	}
}
