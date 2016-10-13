package com.doohh.akkaClustering.master;

import java.util.Hashtable;

import com.doohh.akkaClustering.util.Command;
import com.doohh.akkaClustering.util.Node;

import akka.actor.Address;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class ResourceMngr extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	private Hashtable<Address, Node> workers = new Hashtable<Address, Node>();
	
	@Override
	public void onReceive(Object message) throws Throwable {
		if(message instanceof Command){
			Command cmd = (Command)message;
			log.info("received command: {}", cmd);
			if(cmd.getCommand().equals("putNode()")){
				Node node = (Node)cmd.getData();
				workers.put(node.getActorRef().path().address(), node);
				log.info("put the node to workerTable");
				log.info("current workerTable: {}", workers);
			}
			if(cmd.getClass().equals("removeNode()")){
				Address address = (Address)cmd.getData();
				workers.remove(address);
				log.info("removed the node from workerTable");
				log.info("current workerTable: {}", workers);
			}
			if(cmd.getCommand().equals("getResource()")){
				getSender().tell(workers, getSelf());
			}			
		}
	}
}
