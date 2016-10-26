package com.doohh.akkaClustering.worker;

import org.deeplearning4j.optimize.api.StepFunction;
import org.deeplearning4j.optimize.stepfunctions.NegativeGradientStepFunction;
import org.nd4j.linalg.api.ndarray.INDArray;

import com.doohh.akkaClustering.dto.Command;
import com.doohh.nn.DistMultiLayerNetwork;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class PComm extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	private DistMultiLayerNetwork dmln = null;
	private int iteration = 0;
	@Override
	public void onReceive(Object message) throws Throwable {
		if (message instanceof Command) {
			Command cmd = (Command) message;
			if (cmd.getCommand().equals("setComm()")) {
				log.info("set Comm with DistMultiLayerNetwork");
				dmln = (DistMultiLayerNetwork) cmd.getData();
			}

			if (cmd.getCommand().equals("pushGradient()")) {
				ActorRef ar = getSender();
				log.info("get gradient from slave: {}", getSender());
				INDArray gradient = (INDArray) cmd.getData();
				update(gradient);
				
				
				//getSender().tell(dmln.params(), getSelf());
				//System.out.println("prev send");
				INDArray params = dmln.params();
				//System.out.println(params);
				getSender().tell(params, getSelf());
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

	void update(INDArray gradient) {
		//System.out.println("update");
		//System.out.println("iteration " + iteration++);
		if (gradient != null) {
			System.out.println("no null");
			INDArray params = this.dmln.params();
			StepFunction stepFunction = new NegativeGradientStepFunction();
			stepFunction.step(params, gradient);
			this.dmln.setParams(gradient);
		}
	}
}
