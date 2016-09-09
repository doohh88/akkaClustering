package com.doohh.example;

import java.util.concurrent.TimeUnit;

import org.datavec.image.loader.CifarLoader;
import org.deeplearning4j.datasets.iterator.impl.CifarDataSetIterator;
import org.deeplearning4j.earlystopping.EarlyStoppingConfiguration;
import org.deeplearning4j.earlystopping.EarlyStoppingResult;
import org.deeplearning4j.earlystopping.listener.EarlyStoppingListener;
import org.deeplearning4j.earlystopping.saver.LocalFileModelSaver;
import org.deeplearning4j.earlystopping.scorecalc.DataSetLossCalculator;
import org.deeplearning4j.earlystopping.termination.MaxEpochsTerminationCondition;
import org.deeplearning4j.earlystopping.termination.MaxTimeIterationTerminationCondition;
import org.deeplearning4j.earlystopping.trainer.EarlyStoppingTrainer;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.conf.layers.setup.ConvolutionLayerSetup;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.ui.weights.HistogramIterationListener;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dataset url:
 * https://s3.amazonaws.com/dl4j-distribution/cifar-small.bin
 */
public class Cifar {
    private static final Logger log = LoggerFactory.getLogger(Cifar.class);
    public static void main(String[] args) {
		Util.load("libopenblas");
    	
        int nChannels = 3;
        int outputNum = 10;
        //int numSamples = 2000;
        int numSamples = 2000;
        int batchSize = 128;//500;
        int iterations = 10;
        int splitTrainNum = (int) (batchSize*.8);
        int seed = 123;
        int listenerFreq = iterations/5;

        
        
        DataSetIterator mnistTrain = new CifarDataSetIterator(batchSize, CifarLoader.NUM_TRAIN_IMAGES, new int[]{32, 32, 3}, true);
        DataSetIterator mnistTest = new CifarDataSetIterator(batchSize, CifarLoader.NUM_TEST_IMAGES, new int[]{32, 32, 3}, false);
        /*DataSetIterator mnistTrain = new CifarDataSetIterator(batchSize, 2000, new int[]{32, 32, 3}, true);
        DataSetIterator mnistTest = new CifarDataSetIterator(batchSize, 2000, new int[]{32, 32, 3}, false);*/
        
        //setup the network
        MultiLayerConfiguration.Builder builder = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .iterations(iterations).regularization(true)
                .l1(1e-1).l2(2e-4).useDropConnect(true)
                .gradientNormalization(GradientNormalization.RenormalizeL2PerLayer) // TODO confirm this is required
                .miniBatch(true)
                .optimizationAlgo(OptimizationAlgorithm.CONJUGATE_GRADIENT)
                .list(6)
                .layer(0, new ConvolutionLayer.Builder(5, 5)
                        .nOut(5).dropOut(0.5)
                        .stride(2, 2)
                        .weightInit(WeightInit.XAVIER)
                        .activation("relu")
                        .build())
                .layer(1, new SubsamplingLayer
                        .Builder(SubsamplingLayer.PoolingType.MAX, new int[]{2, 2})
                        .build())
                .layer(2, new ConvolutionLayer.Builder(3, 3)
                        .nOut(10).dropOut(0.5)
                        .stride(2, 2)
                        .weightInit(WeightInit.XAVIER)
                        .activation("relu")
                        .build())
                .layer(3, new SubsamplingLayer
                        .Builder(SubsamplingLayer.PoolingType.MAX, new int[]{2, 2})
                        .build())
                .layer(4, new DenseLayer.Builder().nOut(100).activation("relu")
                        .build())
                .layer(5, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nOut(outputNum)
                        .weightInit(WeightInit.XAVIER)
                        .activation("softmax")
                        .build())
                .backprop(true).pretrain(false);

        new ConvolutionLayerSetup(builder,32,32,nChannels);
        MultiLayerConfiguration conf = builder.build();
        /*MultiLayerNetwork network = new MultiLayerNetwork(conf);
        network.init();*/
        //network.fit(dataSet);
        EarlyStoppingConfiguration esConf = new EarlyStoppingConfiguration.Builder()
				.epochTerminationConditions(new MaxEpochsTerminationCondition(1000))
				.iterationTerminationConditions(new MaxTimeIterationTerminationCondition(20, TimeUnit.MINUTES))
				.scoreCalculator(new DataSetLossCalculator(mnistTest, true)).evaluateEveryNEpochs(1)
				.modelSaver(new LocalFileModelSaver("C:/bestmodel")).build();
		EarlyStoppingTrainer trainer = new EarlyStoppingTrainer(esConf, conf, mnistTrain);
		EarlyStoppingResult result = trainer.fit();
		// Print out the results:
		System.out.println("Termination reason: " + result.getTerminationReason());
		System.out.println("Termination details: " + result.getTerminationDetails());
		System.out.println("Total epochs: " + result.getTotalEpochs());
		System.out.println("Best epoch number: " + result.getBestModelEpoch());
		System.out.println("Score at best epoch: " + result.getBestModelScore());
        
		// Get the best model:
		MultiLayerNetwork bestModel = (MultiLayerNetwork) result.getBestModel();
		
		Evaluation eval = new Evaluation(outputNum);
        while(mnistTest.hasNext()){
            DataSet ds = mnistTest.next();
            INDArray output = bestModel.output(ds.getFeatureMatrix());
            eval.eval(ds.getLabels(), output);
        }
        log.info(eval.stats());
        log.info("****************Example finished********************");
		
        
        /*log.info("Train model....");
        int nEpochs = 1;
        network.setListeners(new ScoreIterationListener(1));
        for( int i=0; i<nEpochs; i++ ) {
        	network.fit(mnistTrain);
            log.info("*** Completed epoch {} ***", i);

            log.info("Evaluate model....");
            Evaluation eval = new Evaluation(outputNum);
            while(mnistTest.hasNext()){
                DataSet ds = mnistTest.next();
                INDArray output = network.output(ds.getFeatureMatrix());
                eval.eval(ds.getLabels(), output);
            }
            log.info(eval.stats());
            mnistTest.reset();
        }
        log.info("****************Example finished********************");*/
    }


}