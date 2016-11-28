package com.doohh.example;

import java.io.File;
import java.util.Arrays;

import org.datavec.image.loader.CifarLoader;
import org.deeplearning4j.datasets.iterator.impl.CifarDataSetIterator;
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
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cifar6 {
	private static final Logger log = LoggerFactory.getLogger(Cifar6.class);

	public static void main(String[] args) {
        //DataSet dataSet = new DataSet();
        // dataSet.load(new File("/home/agibsonccc/Downloads/cifar-train.bin"));
        //dataSet.load(new File("cifar-small.bin"));
        //System.out.println(Arrays.toString(dataSet.getFeatureMatrix().shape()));
		
        int nChannels = 3;
        int outputNum = 10;
        int numSamples = 2000;
        int batchSize = 500;
        int iterations = 10;
        int splitTrainNum = (int) (batchSize*.8);
        int seed = 123;
        int listenerFreq = iterations/5;
        int nEpochs = 10;
        
//	    CifarDataSetIterator train = new CifarDataSetIterator(batchSize, CifarLoader.NUM_TRAIN_IMAGES, true);
//	    CifarDataSetIterator test = new CifarDataSetIterator(batchSize, CifarLoader.NUM_TEST_IMAGES, false);
        
	    CifarDataSetIterator train = new CifarDataSetIterator(batchSize, numSamples, true);
	    CifarDataSetIterator test = new CifarDataSetIterator(batchSize, numSamples, false);
	    
        //setup the network
        MultiLayerConfiguration.Builder builder = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .iterations(iterations).regularization(true)
                .l1(1e-1).l2(2e-4).useDropConnect(true)
                .gradientNormalization(GradientNormalization.RenormalizeL2PerLayer) // TODO confirm this is required
                .miniBatch(true)
                .optimizationAlgo(OptimizationAlgorithm.CONJUGATE_GRADIENT)
                .list()
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
        MultiLayerNetwork network = new MultiLayerNetwork(conf);
        //network.fit(dataSet);
        
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
}
