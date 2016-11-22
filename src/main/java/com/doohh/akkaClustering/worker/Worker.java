package com.doohh.akkaClustering.worker;

import java.util.Hashtable;

import com.doohh.akkaClustering.dto.AppConf;
import com.doohh.akkaClustering.dto.Command;
import com.doohh.akkaClustering.dto.Node;
import com.doohh.akkaClustering.master.Master;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
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
	private ActorRef task = null;
	private ActorSelection master = null;

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
			// masters.put(getSender().path().address(), new Node(getSender(),
			// false));
			log.info("current masterTable: {}", masters);
		} else if (message instanceof MemberUp) {
			log.info("received MemberUp msg");
			MemberUp mUp = (MemberUp) message;
			log.info("send the msg to the master for handshaking");
			register(mUp.member());
		} else if (message instanceof MemberEvent) {
		}

		else if (message instanceof Command) {
			Command cmd = (Command) message;
			if (cmd.getCommand().equals("submit()")) {
				getSender().tell("receive appConf from launcher", getSelf());
				AppConf appConf = (AppConf) cmd.getData();
				log.info("get appConf from master : {}", appConf);
				task = context().actorOf(Props.create(Task.class), "task");
				log.info("generate task for proc");
				log.info("getSender: {}", getSender());
				// task.tell(appConf, getSender());
				task.tell(new Command().setCommand("runApp()").setData(appConf), getSender());
			}
			if (cmd.getCommand().equals("stopTask()")) {
				log.info("stopTask()");
				context().stop(task);
				this.master.tell(new Command().setCommand("returnResource()").setData(null), getSelf());
				// getSender().tell("stopped Task", getSelf());
			}
		}

		else if (message instanceof String) {
			log.info("received msg = {}", (String) message);
		} else {
			log.info("received unhandled msg");
			unhandled(message);
		}

	}

	void register(Member member) {
		if (member.hasRole("master")) {
			this.master = getContext().actorSelection(member.address() + "/user/master");
			this.master.tell(REGISTRATION_TO_WORKER, getSelf());
			// getContext().actorSelection(member.address() +
			// "/user/master").tell(REGISTRATION_TO_WORKER, getSelf());
		}
	}
}
