package com.doohh.example;

import java.io.IOException;

import org.datavec.image.loader.CifarLoader;
import org.deeplearning4j.datasets.iterator.impl.CifarDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cifar7 {
	private static final Logger log = LoggerFactory.getLogger(Cifar7.class);

	@Option(name="--batchSize",usage="batchSize",aliases = "-b")
    int batchSize = 64;
    @Option(name="--nEpochs",usage="nEpochs",aliases = "-e")
    int nEpochs = 1;
    @Option(name="--iterations",usage="iterations",aliases = "-i")
    int iterations = 1;
	
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
    
	void run(String[] args) throws IOException {
		this.parseArgs(args);
		
		int nChannels = 3; // Number of input channels
		int outputNum = 10; // The number of possible outcomes
		int seed = 123; //
		int numExamples = 60000;
		/*
		 * Create an iterator using the batch size for one iteration
		 */
		log.info("Load data....");

		
	    DataSetIterator train = new CifarDataSetIterator(batchSize, CifarLoader.NUM_TRAIN_IMAGES, true);
	    DataSetIterator test = new CifarDataSetIterator(batchSize, CifarLoader.NUM_TEST_IMAGES, false);

		/*
		 * Construct the neural network
		 */
		log.info("Build model....");
		 MultiLayerConfiguration.Builder builder = new NeuralNetConfiguration.Builder()
					.seed(seed)
					.iterations(iterations)
					.gradientNormalization(GradientNormalization.RenormalizeL2PerLayer)
					.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
					.list()
					.layer(0, new ConvolutionLayer.Builder(5, 5)
							.nIn(3)
							.nOut(5)
							.weightInit(WeightInit.XAVIER)
							.activation("relu")
							.build())
					.layer(1, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX, new int[] {2,2})
							.build())
					.layer(2, new ConvolutionLayer.Builder(3, 3)
							.nOut(10)
							.weightInit(WeightInit.XAVIER)
							.activation("relu")
							.build())
					.layer(3, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX, new int[] {2,2})
							.build())
					.layer(4, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
							.nOut(outputNum)
							.weightInit(WeightInit.XAVIER)
							.activation("softmax")
							.build())
					.backprop(true).pretrain(false)
					.cnnInputSize(32, 32, 3);

			MultiLayerNetwork network = new MultiLayerNetwork(builder.build());
			network.init();

		nEpochs = 10;
		
        log.error("Train model....");
        network.setListeners(new ScoreIterationListener(1));
        for( int i=0; i<nEpochs; i++ ) {
        	network.fit(train);
            log.error("*** Completed epoch {} ***", i); 

            log.error("Evaluate model....");
            Evaluation eval = new Evaluation();
            test.reset();
            while(test.hasNext()){
                DataSet ds = test.next();
                INDArray output = network.output(ds.getFeatureMatrix(), false);
                eval.eval(ds.getLabels(), output);
            }
            log.error(eval.stats());
        }
        log.error("****************Example finished********************");
	}
	

	public static void main(String[] args) {
		System.out.println("start...");
		try {
			new Cifar7().run(args);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("finish...");
	}
}
