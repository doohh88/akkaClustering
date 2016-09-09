package com.doohh.akkaClustering.master;

import java.util.HashMap;

import org.deeplearning4j.datasets.iterator.impl.CifarDataSetIterator;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

import com.doohh.akkaClustering.deploy.Launcher;
import com.doohh.akkaClustering.deploy.UserAppConf;
import com.doohh.akkaClustering.worker.Worker;

import akka.actor.ActorRef;
import akka.actor.Address;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.UnreachableMember;
import akka.cluster.Member;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import lombok.Getter;

@Getter
public class Master extends UntypedActor {
	public static final String MASTER_REGISTRATION = "MasterRegistration";
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	Cluster cluster = Cluster.get(getContext().system());
	HashMap<Address, ActorRef> workers = new HashMap<Address, ActorRef>();
	UserAppConf userAppConf = null;
	private ActorRef launcher;

	// subscribe to cluster changes, MemberUp
	@Override
	public void preStart() {
		cluster.subscribe(getSelf(), MemberUp.class, UnreachableMember.class);
		this.launcher = context().actorOf(Props.create(Launcher.class), "launcher");
	}

	// re-subscribe when restart
	@Override
	public void postStop() {
		cluster.unsubscribe(getSelf());

	}

	@Override
	public void onReceive(Object message) throws Throwable {
		// TODO Auto-generated method stub
		if (message.equals(Worker.WORKER_REGISTRATION)) {
			getContext().watch(getSender());
			workers.put(getSender().path().address(), getSender());
		} else if (message instanceof MemberUp) {
			MemberUp mUp = (MemberUp) message;
			register(mUp.member());
		} else if (message instanceof UnreachableMember) {
			UnreachableMember mUnreachable = (UnreachableMember) message;
			workers.remove(mUnreachable.member().address());
		}

		else if (message instanceof UserAppConf) {
			userAppConf = (UserAppConf) message;
			launcher.tell(userAppConf, getSelf());
			getSender().tell("received UserAppConf instance", getSelf());
		}

		else if (message.equals("getWorkers")) {
			getSender().tell(workers, getSelf());
		}

		else if (message instanceof String) {
			log.info("Get message = {}", (String) message);
		} else {
			unhandled(message);
		}
	}

	void register(Member member) {
		if (member.hasRole("worker")) {
			getContext().actorSelection(member.address() + "/user/worker").tell(MASTER_REGISTRATION, getSelf());
		}
	}
}
