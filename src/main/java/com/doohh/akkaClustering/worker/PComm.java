package com.doohh.akkaClustering.worker;

import org.deeplearning4j.nn.gradient.Gradient;
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
	// private DistMultiLayerNetwork dmln = null;
	private INDArray param = null;
	private int idx;
	private int iteration = 0;
	private DistMultiLayerNetwork dmln = null;
	
	public PComm(DistMultiLayerNetwork dmln) {
		this.dmln = dmln;
	}
	
	
	@Override
	public void onReceive(Object message) throws Throwable {
		if (message instanceof Command) {
			Command cmd = (Command) message;

			if (cmd.getCommand().equals("setParam()")) {
				log.info("set PComm with parameters");
				param = (INDArray) cmd.getData();
				
			}

			if (cmd.getCommand().equals("checkParamInit()")) {
				log.info("checkParamInit()");
				if (param != null)
					getSender().tell("true", getSelf());
				else
					getSender().tell("false", getSelf());
			}

			if (cmd.getCommand().equals("pushGradient()")) {
				ActorRef ar = getSender();
				log.info("get gradient from slave: {}", getSender());
				Gradient gradient = (Gradient) cmd.getData();
				update(gradient);
				getSender().tell(this.param, getSelf());
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

	void update(Gradient gradient) {
		System.out.println("iteration " + iteration++);
		if (gradient != null) {
			StepFunction stepFunction = new NegativeGradientStepFunction();
			stepFunction.step(this.param, gradient.gradient());
		}
	}
}
