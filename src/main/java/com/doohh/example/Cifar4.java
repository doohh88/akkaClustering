package com.doohh.example;

import org.datavec.image.loader.CifarLoader;
import org.deeplearning4j.datasets.iterator.impl.CifarDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.conf.layers.setup.ConvolutionLayerSetup;
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

public class Cifar4 {
private static final Logger log = LoggerFactory.getLogger(Cifar4.class);
	
	@Option(name = "--batchSize", usage = "batchSize", aliases = "-b")
	int batchSize = 128;
	@Option(name = "--nEpochs", usage = "nEpochs", aliases = "-e")
	int nEpochs = 1;
	@Option(name = "--iterations", usage = "iterations", aliases = "-i")
	int iterations = 1;
	@Option(name = "--listenerFreq", usage = "listenerFreq", aliases = "-l")
	int listenerFreq = 1;
	@Option(name = "--numTrain", usage = "numTrain", aliases = "-tr")
	int numTrain = CifarLoader.NUM_TRAIN_IMAGES;
	@Option(name = "--numTest", usage = "numTest", aliases = "-te")
	int numTest = CifarLoader.NUM_TEST_IMAGES;

    
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
	
	void run(String[] args) {
		// TODO Auto-generated method stub
		this.parseArgs(args);
		
		int nChannels = 3;
	    int outputNum = 10;
	    int splitTrainNum = (int) (batchSize*.8);
	    int seed = 123;
		
	    batchSize = 5;
        DataSetIterator train = new CifarDataSetIterator(batchSize, numTrain, true);
        DataSetIterator test = new CifarDataSetIterator(batchSize, numTest, false);
                
        
        //setup the network
        MultiLayerConfiguration.Builder builder = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .iterations(iterations)
                .regularization(true).l2(0.0005)
                .learningRate(0.01)
                .weightInit(WeightInit.XAVIER)
                .optimizationAlgo(OptimizationAlgorithm.LINE_GRADIENT_DESCENT)
                .updater(Updater.NESTEROVS).momentum(0.9)
                .list()
                .layer(0, new ConvolutionLayer.Builder(5, 5)
                        .nIn(nChannels)
                        .stride(1, 1)
                        .nOut(20)
                        .activation("identity")
                        .build())
                .layer(1, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2,2)
                        .stride(2,2)
                        .build())
                .layer(2, new ConvolutionLayer.Builder(5, 5)
                        .nIn(nChannels)
                        .stride(1, 1)
                        .nOut(50)
                        .activation("identity")
                        .build())
                .layer(3, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2,2)
                        .stride(2,2)
                        .build())
                .layer(4, new DenseLayer.Builder().activation("relu")
                        .nOut(500).build())
                .layer(5, new OutputLayer.Builder(LossFunctions.LossFunction.XENT)
                        .nOut(outputNum)
                        .activation("softmax")
                        .build())
                .backprop(true).pretrain(false);
        new ConvolutionLayerSetup(builder,32,32,3);

        MultiLayerConfiguration conf = builder.build();
        MultiLayerNetwork network = new MultiLayerNetwork(conf);

        network.setListeners(new ScoreIterationListener(listenerFreq));
        
        log.error("Train model....");
        network.setListeners(new ScoreIterationListener(listenerFreq));
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
		new Cifar4().run(args);	
		System.out.println("finish...");
	}
}
