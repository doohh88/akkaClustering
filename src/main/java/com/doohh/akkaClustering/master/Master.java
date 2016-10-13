package com.doohh.akkaClustering.master;

import java.util.Hashtable;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;

import com.doohh.akkaClustering.deploy.AppConf;
import com.doohh.akkaClustering.util.Node;
import com.doohh.akkaClustering.worker.Worker;

import akka.actor.ActorRef;
import akka.actor.Address;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.MemberRemoved;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.UnreachableMember;
import akka.cluster.Member;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import lombok.Getter;

@Getter
public class Master extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	public static final String REGISTRATION_TO_MASTER = "Master registrate the worker";
	private Cluster cluster = Cluster.get(getContext().system());
	private Hashtable<Address, Node> workers = new Hashtable<Address, Node>();
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
		// clustering part
		if (message.equals(Worker.REGISTRATION_TO_WORKER)) {
			log.info("received registration msg from the worker");
			log.info("register the worker at master");
			Node node = new Node(getSender(), false);
			// workers.put(getSender().path().address(), new Node(getSender(),
			// false));
			workers.put(getSender().path().address(), node);
			log.info("current workerTable: {}", workers);

			// nd4j -> workers
			// INDArray a = Nd4j.create(new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9,
			// 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, new int[]{2, 10});
			// INDArray b = a.get(NDArrayIndex.interval(0, 1),
			// NDArrayIndex.interval(0, 5));
			// node.getActorRef().tell(b, getSelf());

		} else if (message instanceof MemberUp) {
			log.info("received MemberUp msg");
			MemberUp mUp = (MemberUp) message;
			log.info("send the msg to the worker for handshaking");
			register(mUp.member());
		} else if (message instanceof UnreachableMember) {
			log.info("received UnreachableMember msg");
			log.info("remove the worker from master");
			UnreachableMember mUnreachable = (UnreachableMember) message;
			workers.remove(mUnreachable.member().address());
			log.info("current workerTable: {}", workers);
		} else if (message instanceof MemberRemoved) {
			log.info("received MemberRemoved msg");
			MemberRemoved mRemoved = (MemberRemoved) message;
			log.info("Member is Removed: {}", mRemoved.member());
		} else if (message instanceof MemberEvent) {
		}

		// deploy part
		else if (message instanceof AppConf) {
			AppConf appConf = (AppConf) message;
			log.info("receive appConf msg: {}", appConf);
			log.info("getSelf: {}", getSelf());
			launcher.tell(appConf, getSelf());
			log.info("send appConf to master");
			getSender().tell("received UserAppConf instance", getSelf());
		}

		// query
		else if (message.equals("getWorkers()")) {
			getSender().tell(workers, getSelf());
		}

		// query
		else if (message.equals("getWorkers()")) {
			getSender().tell(workers, getSelf());
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
		if (member.hasRole("worker")) {
			getContext().actorSelection(member.address() + "/user/worker").tell(REGISTRATION_TO_MASTER, getSelf());
		}
	}
}
