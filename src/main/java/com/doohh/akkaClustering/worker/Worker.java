package com.doohh.akkaClustering.worker;

import java.util.ArrayList;
import java.util.List;

import com.doohh.akkaClustering.master.Master;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent.MemberUp;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.cluster.Member;

public class Worker extends UntypedActor {
	public static final String WORKER_REGISTRATION = "WorkerRegistration";
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	Cluster cluster = Cluster.get(getContext().system());
	List<ActorRef> masters = new ArrayList<ActorRef>();

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
			masters.add(getSender());
			log.info("master list = {}", masters.toString());
		} else if (message instanceof MemberUp) {
			MemberUp mUp = (MemberUp) message;
			register(mUp.member());
		} else {
			unhandled(message);
		}

	}

	void register(Member member) {
		if (member.hasRole("master"))
			getContext().actorSelection(member.address() + "/user/master").tell(WORKER_REGISTRATION, getSelf());
	}
}
