package com.doohh.akkaClustering.worker;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import com.doohh.akkaClustering.dto.Command;
import com.doohh.nn.DistMultiLayerNetwork;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class Agent extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	private DistMultiLayerNetwork dmln = null; 
	
	@Override
	public void onReceive(Object message) throws Throwable {
		if(message instanceof Command){
			Command cmd = (Command) message;
			if(cmd.equals("DistMultiLayerNetwork()")){
				log.info("set DistMultiLayerNetwor in agent");
				dmln = (DistMultiLayerNetwork)cmd.getData();
			}
			
			if(cmd.equals("pushGradient()")){
				System.out.println("pushGradient()");
				System.out.println((INDArray)cmd.getData());
				getSender().tell("hoho", getSelf());
				//INDArray params = Nd4j.create(new float[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12}, new int[]{1, 12});
				//getSender().tell(cmd.setData(params), getSelf());
				//getSender().tell(cmd.setData(dmln.params()), getSelf());
			}
		}
		
		else if (message instanceof String) {
			log.info("received msg = {}", (String) message);
		} else {
			log.info("received unhandled msg");
			unhandled(message);
		}
	}
}
