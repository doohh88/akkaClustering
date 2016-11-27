package com.doohh.akkaClustering.worker;

import org.deeplearning4j.nn.gradient.DefaultGradient;
import org.deeplearning4j.nn.gradient.Gradient;
import org.deeplearning4j.optimize.api.StepFunction;
import org.deeplearning4j.optimize.stepfunctions.NegativeGradientStepFunction;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.indexing.NDArrayIndex;

import com.doohh.akkaClustering.dto.Command;
import com.doohh.akkaClustering.util.Nd4jSerialization;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class PComm extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	private INDArray param;
	private int paramSize = 0;
	private int idx;
	private int nParamServer;
	private int start = 0;
	private int end = 0;
	private int iteration = 0;
	private Nd4jSerialization nd4jSerialization;

	public PComm(int nParamServer, int idx) {
		this.idx = idx;
		this.nParamServer = nParamServer;
		nd4jSerialization = new Nd4jSerialization();		
		
		// for loading ND4j lib
		//this.param = Nd4j.zeros(1);
		//System.out.println("load nd4jlib " + this.param);
		//log.error("load nd4jlib {}", this.param);
	}

	@Override
	public void onReceive(Object message) throws Throwable {
		if (message instanceof Command) {
			Command cmd = (Command) message;

			if (cmd.getCommand().equals("initParam()")) {
				log.info("init PComm with parameters");
				this.param = (INDArray) nd4jSerialization.deserialize((byte[])cmd.getData());
				log.error("{}", this.param.get(NDArrayIndex.interval(0, 1), NDArrayIndex.interval(0, 10)));
				getSender().tell("setting", getSelf());
			}

			if (cmd.getCommand().equals("pushGradient()")) {
				// ActorRef ar = getSender();
				log.info("get gradient from slave: {}", getSender());
				INDArray gradient = (INDArray) cmd.getData();
				update(gradient);
				getSender().tell(this.param, getSelf());
				log.info("send parameter to slave: {}", getSender());
			}

			if (cmd.getCommand().equals("setParam()")) {
				// System.out.println("setParam");
				log.info("set PComm with parameters");
				this.param = (INDArray) cmd.getData();
				getSender().tell("setting", getSelf());
			}

			if (cmd.getCommand().equals("pullParam()")) {
				// System.out.println("pullParam");
				log.info("send parameters to slave from pcomm");
				if (this.param != null)
					getSender().tell(this.param, getSelf());
				else
					log.info("NULLLLLL");
				// System.out.println("NULLLLLL");
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

	// void update(Gradient gradient) {
	void update(INDArray grad) {
		// System.out.println("paramSize: " + this.param.size(1));
		// System.out.println("gradSize: " + grad.size(1));
		Gradient gradient = new DefaultGradient(grad);
		// System.out.println("iteration " + iteration++);
//		System.out.println(grad.get(NDArrayIndex.interval(0, 1), NDArrayIndex.interval(0, 10)));
//		System.out.println(this.param.get(NDArrayIndex.interval(0, 1), NDArrayIndex.interval(0, 10)));
		if (gradient != null) {
			StepFunction stepFunction = new NegativeGradientStepFunction();
			stepFunction.step(this.param, gradient.gradient());
		}
	}
}
