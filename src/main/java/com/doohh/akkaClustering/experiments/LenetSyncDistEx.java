package com.doohh.akkaClustering.experiments;

import java.io.IOException;

import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.PerformanceListener;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.doohh.akkaClustering.dto.AppConf;
import com.doohh.akkaClustering.dto.DistInfo;
import com.doohh.akkaClustering.nn.DistMnistDataSetIterator;
import com.doohh.akkaClustering.nn.SyncDistMultiLayerNetwork;
import com.doohh.akkaClustering.worker.Controller;

public class LenetSyncDistEx {
	private static final Logger log = LoggerFactory.getLogger(LenetSyncDistEx.class);

	@Option(name = "--batchSize", usage = "batchSize", aliases = "-b")
	int batchSize = 128;
	@Option(name = "--nEpochs", usage = "nEpochs", aliases = "-e")
	int nEpochs = 1;
	@Option(name = "--iterations", usage = "iterations", aliases = "-i")
	int iterations = 1;
	@Option(name = "--listenerFreq", usage = "listenerFreq", aliases = "-l")
	int listenerFreq = 1;
	private AppConf appConf;

	private void parseArgs(String[] args) {
		CmdLineParser parser = new CmdLineParser(this);
		try {
			parser.parseArgument(args);
			log.error("batchSize : {}", batchSize);
			log.error("nEpochs: {}", nEpochs);
			log.error("iterations: {}", iterations);
		} catch (CmdLineException e) {
			// handling of wrong arguments
			log.error(e.getMessage());
			parser.printUsage(System.err);
		}

	}

	void run(AppConf appConf, String[] args) throws IOException {
		this.parseArgs(args);

		int nChannels = 1; // Number of input channels
		int outputNum = 10; // The number of possible outcomes
		int seed = 123; //
		int numExamples = 0;
		DistInfo distInfo = null;
		DataSetIterator mnistTrain = null;
		DataSetIterator mnistTest = null;

		/*
		 * Create an iterator using the batch size for one iteration
		 */
		log.info("Load distInfo....");
		distInfo = new DistInfo();
		distInfo.init(appConf);

		log.info("Load data....");
		if (distInfo.getRole().equals("slave")) {
			numExamples = distInfo.getNumExamples("DistMnistDataFetcher");
			mnistTrain = new DistMnistDataSetIterator(batchSize, numExamples, false, true, true, 12345, distInfo);
			if (distInfo.getRoleIdx() == 0)
				mnistTest = new MnistDataSetIterator(batchSize, false, 12345);

			/*
			 * Construct the neural network
			 */
			log.info("Build model....");
			MultiLayerConfiguration.Builder builder = new NeuralNetConfiguration.Builder().seed(seed)
					.iterations(iterations).regularization(true).l2(0.0005)
					/*
					 * Uncomment the following for learning decay and bias
					 */
					.learningRate(.01)// .biasLearningRate(0.02)
					// .learningRateDecayPolicy(LearningRatePolicy.Inverse).lrPolicyDecayRate(0.001).lrPolicyPower(0.75)
					.weightInit(WeightInit.XAVIER).optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
					.updater(Updater.NESTEROVS).momentum(0.9).list()
					.layer(0,
							new ConvolutionLayer.Builder(5, 5).nIn(nChannels).stride(1, 1).nOut(20)
									.activation("identity").build())
					.layer(1,
							new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX).kernelSize(2, 2).stride(2, 2)
									.build())
					.layer(2, new ConvolutionLayer.Builder(5, 5).stride(1, 1).nOut(50).activation("identity").build())
					.layer(3,
							new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX).kernelSize(2, 2).stride(2, 2)
									.build())
					.layer(4, new DenseLayer.Builder().activation("relu").nOut(500).build())
					.layer(5,
							new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD).nOut(outputNum)
									.activation("softmax").build())
					.setInputType(InputType.convolutionalFlat(28, 28, 1)).backprop(true).pretrain(false);

			MultiLayerConfiguration conf = builder.build();
			SyncDistMultiLayerNetwork model = new SyncDistMultiLayerNetwork(conf, distInfo);
			model.init();
			model.setListeners(new ScoreIterationListener(listenerFreq), new PerformanceListener(listenerFreq));

			log.error("Train model....");
			long startTime = System.currentTimeMillis();
			for (int i = 0; i < nEpochs; i++) {
				model.fit(mnistTrain);
				log.error("*** Completed epoch {} ***", i);
			}
			long endTime = System.currentTimeMillis();
			log.error("time: {}", endTime - startTime);

			if (distInfo.getRoleIdx() == 0) {
				log.error("Evaluate model....");
				Evaluation eval = new Evaluation();
				while (mnistTest.hasNext()) {
					DataSet ds = mnistTest.next();
					INDArray output = model.output(ds.getFeatureMatrix(), false);
					eval.eval(ds.getLabels(), output);
				}
				log.error(eval.stats());
				log.error("****************Example finished********************");
				model.finishApp(appConf);
			}
			Controller.barrier(distInfo, "slave");
			if (distInfo.getRoleIdx() == 0)
				model.finishApp(appConf);
		}
	}

	public static void main(AppConf appConf, String[] args) {
		System.out.println("start...");
		try {
			new LenetSyncDistEx().run(appConf, args);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("finish...");
	}
}
