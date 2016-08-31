package com.doohh.akkaClustering.master;

import java.util.ArrayList;
import java.util.List;

import com.doohh.akkaClustering.deploy.UserAppConf;
import com.doohh.akkaClustering.worker.Worker;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.Member;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class Master extends UntypedActor {
	public static final String MASTER_REGISTRATION = "MasterRegistration";
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	Cluster cluster = Cluster.get(getContext().system());
	List<ActorRef> workers = new ArrayList<ActorRef>();
	UserAppConf userAppConf = null;
	
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
		// TODO Auto-generated method stub
		if(message instanceof String){
			String msg = (String)message;
			log.info("get message : {}", msg);
		} else if (message.equals(Worker.WORKER_REGISTRATION)) {
			getContext().watch(getSender());
			workers.add(getSender());
			log.info("worker list = {}", workers.toString());
		} else if (message instanceof MemberUp) {
			MemberUp mUp = (MemberUp) message;
			register(mUp.member());
		} else if(message instanceof UserAppConf){
			userAppConf = (UserAppConf)message;
			log.info("get userAppConf({}, {})", userAppConf.getPackagePath(), userAppConf.getMainClass());
		} else {
			unhandled(message);
		}
	}

	void register(Member member) {
		if (member.hasRole("worker")) {
			getContext().actorSelection(member.address() + "/user/worker").tell(MASTER_REGISTRATION, getSelf());
			log.info("worker(member.address()) : {}", member.address());
		}
	}
}
