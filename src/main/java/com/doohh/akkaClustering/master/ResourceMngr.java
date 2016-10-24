package com.doohh.akkaClustering.master;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.doohh.akkaClustering.dto.AppConf;
import com.doohh.akkaClustering.dto.Command;
import com.doohh.akkaClustering.dto.Node;
import com.doohh.akkaClustering.dto.RouterInfo;

import akka.actor.ActorRef;
import akka.actor.Address;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.RoundRobinGroup;

public class ResourceMngr extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	private Hashtable<Address, Node> workers = new Hashtable<Address, Node>();

	@Override
	public void onReceive(Object message) throws Throwable {
		if (message instanceof Command) {
			Command cmd = (Command) message;
			log.info("received command: {}", cmd);
			if (cmd.getCommand().equals("putNode()")) {
				Node node = (Node) cmd.getData();
				workers.put(node.getActorRef().path().address(), node);
				log.info("put the node to workerTable");
				log.info("current workerTable: {}", workers);
			}
			if (cmd.getClass().equals("removeNode()")) {
				Address address = (Address) cmd.getData();
				workers.remove(address);
				log.info("removed the node from workerTable");
				log.info("current workerTable: {}", workers);
			}
			if (cmd.getCommand().equals("setProcFalse()")) {
				workers.get((Address) cmd.getData()).setProc(false);
				log.info("workers : {}", workers);
			}

			if (cmd.getCommand().equals("getResource()")) {
				RouterInfo routerInfo = selectNodes((AppConf) cmd.getData());
				getSender().tell(routerInfo, getSelf());
			}
		}
	}

	public RouterInfo selectNodes(AppConf appConf) {
		List<String> routeePaths = new ArrayList<String>();
		RouterInfo routerInfo = new RouterInfo();
		int nParamNode = appConf.getNMaster();
		int nSlaveNode = appConf.getNWorker();
		int nProcNodes = nParamNode + nSlaveNode;

		routerInfo.setNNodes(nProcNodes);
		log.info("select {} nodes for proc");
		int i = 0;
		for (Node node : workers.values()) {
			if (node.isProc() == false) {
				String addr = node.getActorRef().path().toString();
				routeePaths.add(addr);
				node.setProc(true);
				log.info("set node state by true: {}", node);

				if (i < nParamNode) {
					routerInfo.getParamAddr().add(addr);
				} else {
					routerInfo.getSlaveAddr().add(addr);
				}

				if (routeePaths.size() == nProcNodes)
					break;
				i++;
			}
		}
		log.info("routeePaths : {}", routeePaths);
		//router's name : router + currentTime
		ActorRef router = getContext().actorOf(new RoundRobinGroup(routeePaths).props(),
				"router" + System.currentTimeMillis());
		routerInfo.setRouter(router);
		return routerInfo;
	}
}
