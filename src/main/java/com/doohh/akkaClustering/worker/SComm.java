package com.doohh.akkaClustering.worker;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import com.doohh.akkaClustering.dto.Command;
import com.doohh.nn.DistMultiLayerNetwork;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class SComm extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	private DistMultiLayerNetwork dmln = null;
	private INDArray param = null;

	@Override
	public void onReceive(Object message) throws Throwable {
		if (message instanceof Command) {
			Command cmd = (Command) message;
			if(cmd.getCommand().equals("setComm()")){
				log.info("set Comm with DistMultiLayerNetwork");
				dmln = (DistMultiLayerNetwork)cmd.getData();				 
			}
			
			if (cmd.getCommand().equals("setParam()")) {
				log.info("set PComm with parameters");
				param = (INDArray) cmd.getData();
			}
			
			
			if (cmd.getCommand().equals("waitSlave()")) {
				log.info("wait slave");
				dmln = (DistMultiLayerNetwork)cmd.getData();
				synchronized(dmln){
					while(canStart == true){
						dmln.wait();						
					}
				}
				
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
