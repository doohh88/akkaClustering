package com.doohh.akkaClustering.master;

import java.util.Hashtable;

import com.doohh.akkaClustering.dto.Command;
import com.doohh.akkaClustering.dto.Node;
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
	private ActorRef launcher;
	private ActorRef resourceMngr;

	// subscribe to cluster changes, MemberUp
	@Override
	public void preStart() {
		cluster.subscribe(getSelf(), MemberUp.class, UnreachableMember.class);
		this.resourceMngr = context().actorOf(Props.create(ResourceMngr.class), "resourceMngr");
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
			Command cmd = new Command("putNode()", new Node(getSender(), false));
			this.resourceMngr.tell(cmd, getSelf());
			log.info("send command to resourceMngr: {}", cmd);
		} else if (message instanceof MemberUp) {
			log.info("received MemberUp msg");
			MemberUp mUp = (MemberUp) message;
			log.info("send the msg to the worker for handshaking");
			register(mUp.member());
		} else if (message instanceof UnreachableMember) {
			log.info("received UnreachableMember msg");
			log.info("remove the worker from master");
			UnreachableMember mUnreachable = (UnreachableMember) message;
			Command cmd = new Command("removeNode()", mUnreachable.member().address());
			this.resourceMngr.tell(cmd, getSelf());
			log.info("send command to resourceMngr: {}", cmd);
		} else if (message instanceof MemberRemoved) {
			log.info("received MemberRemoved msg");
			MemberRemoved mRemoved = (MemberRemoved) message;
			log.info("Member is Removed: {}", mRemoved.member());
		} else if (message instanceof MemberEvent) {
		}

		// deploy part
		else if (message instanceof Command) {
			Command cmd = (Command) message;
			log.info("received command: {}", cmd);
			if (cmd.getCommand().equals("submit()")) {
				log.info("starting submit()");
				launcher.tell(cmd, getSelf());
				log.info("sended appConf to master");
				getSender().tell("received command and launching applicaiton", getSelf());
			}
		}

		else if (message.equals("finishApp()")) {
			this.resourceMngr.tell(new Command().setCommand("setProcFalse()").setData(getSender().path().address()), getSelf());
			getSender().tell("stopTask()", getSelf());
			log.info("send msg(stopTask) to the TaskActor");
		}

		else if (message instanceof String) {
			log.info("received msg = {}", (String) message);
		} else {
			log.info("received unhandled msg");
			unhandled(message);
		}
	}

	void register(Member member) {
		if (member.hasRole("worker")) {
			getContext().actorSelection(member.address() + "/user/worker").tell(REGISTRATION_TO_MASTER, getSelf());
		}
	}
}
