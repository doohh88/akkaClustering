package com.doohh.akkaClustering.worker;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import com.doohh.akkaClustering.dto.Command;
import com.doohh.nn.DistMultiLayerNetwork;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class PComm extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	private DistMultiLayerNetwork dmln = null;

	@Override
	public void onReceive(Object message) throws Throwable {
		if (message instanceof Command) {
			Command cmd = (Command) message;
			if(cmd.getCommand().equals("setComm()")){
				log.info("set Comm with DistMultiLayerNetwork");
				 dmln = (DistMultiLayerNetwork)cmd.getData();				 
			}
			
			if (cmd.getCommand().equals("pushGradient()")) {
				log.info("get gradient from slave: {}", getSender());
				getSender().tell(dmln.params(), getSelf());			
				log.info("send parameter to slave: {}", getSender());
			}
		}

		else if (message instanceof String) {
			log.info("received msg = {}", (String) message);
			System.out.println(message);
		} else {
			log.info("received unhandled msg");
			unhandled(message);
		}
	}
}
