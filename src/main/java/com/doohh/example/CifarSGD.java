package com.doohh.example;

import java.util.Arrays;
import java.util.List;

import org.datavec.image.loader.CifarLoader;
import org.deeplearning4j.datasets.iterator.impl.CifarDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.conf.preprocessor.CnnToFeedForwardPreProcessor;
import org.deeplearning4j.nn.conf.preprocessor.FeedForwardToCnnPreProcessor;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
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

public class CifarSGD {
private static final Logger log = LoggerFactory.getLogger(CifarSGD.class);
	
	@Option(name="--batchSize",usage="batchSize",aliases = "-b")
    int batchSize = 128;
    @Option(name="--nEpochs",usage="nEpochs",aliases = "-e")
    int nEpochs = 100;
    @Option(name="--numTrain",usage="numTrain",aliases = "-tr")
    int numTrain = CifarLoader.NUM_TRAIN_IMAGES;
    @Option(name="--numTest",usage="numTest",aliases = "-te")
    int numTest = CifarLoader.NUM_TEST_IMAGES;

    
	private void parseArgs(String[] args) {
		CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);
            log.info("batchSize : {}", batchSize);
            log.info("nEpochs: {}", nEpochs);
        } catch (CmdLineException e) {
            // handling of wrong arguments
        	log.info(e.getMessage());
            parser.printUsage(System.err);
        }

	}
	
	void run(String[] args) {
		// TODO Auto-generated method stub
		this.parseArgs(args);
		
		int nChannels = 3;
	    int outputNum = 10;
	    int iterations = 10;
	    int splitTrainNum = (int) (batchSize*.8);
	    int seed = 123;
	    int listenerFreq = iterations/5;
	    List<String> LABELS = Arrays.asList("airplane", "automobile", "bird", "cat", "deer", "dog", "frog", "horse", "ship", "truck");
	    
        DataSetIterator train = new CifarDataSetIterator(batchSize, numTrain, true);
        DataSetIterator test = new CifarDataSetIterator(batchSize, numTest, false);
               
        //setup the network
        MultiLayerConfiguration.Builder builder = new NeuralNetConfiguration.Builder()
        		.seed(seed)
                .iterations(10)
                .momentum(0.9)
                .regularization(true)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .list()
                .layer(0, new ConvolutionLayer.Builder(new int[]{5, 5})
                        .nIn(1)
                        .nOut(20)
                        .stride(new int[]{1, 1})
                        .activation("relu")
                        .weightInit(WeightInit.XAVIER)
                        .build())
                .layer(1, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX, new int[]{2, 2})
                        .build())
                .layer(2, new ConvolutionLayer.Builder(new int[]{5, 5})
                        .nIn(20)
                        .nOut(40)
                        .stride(new int[]{1, 1})
                        .activation("relu")
                        .weightInit(WeightInit.XAVIER)
                        .build())
                .layer(3, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX, new int[]{2, 2})
                        .build())
                .layer(4, new DenseLayer.Builder()
                        .nIn(40 * 5 * 5)
                        .nOut(1000)
                        .activation("relu")
                        .weightInit(WeightInit.XAVIER)
                        .dropOut(0.5)
                        .build())
                .layer(5, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .nIn(1000)
                        .nOut(LABELS.size())
                        .dropOut(0.5)
                        .weightInit(WeightInit.XAVIER)
                        .build())
                .inputPreProcessor(0, new FeedForwardToCnnPreProcessor(32, 32, 1))
                .inputPreProcessor(4, new CnnToFeedForwardPreProcessor())
                .backprop(true).pretrain(false);

        MultiLayerConfiguration conf = builder.build();
        MultiLayerNetwork network = new MultiLayerNetwork(conf);
        network.init();
        network.setListeners(new ScoreIterationListener(listenerFreq));
        log.error("hello");
		for (int i = 0; i < nEpochs; i++) {
			network.fit(train);
			
			Evaluation eval = new Evaluation(outputNum);
			test.reset();
			while (test.hasNext()) {
				DataSet testSet = test.next();
				// log.info("{}", testSet.get(0));
				INDArray output = network.output(testSet.getFeatureMatrix(), false);
				eval.eval(testSet.getLabels(), output);
			}
			log.error(eval.stats());
		}
	}
	
	public static void main(String[] args) { new CifarCompleteEx().run(args);	}
}
