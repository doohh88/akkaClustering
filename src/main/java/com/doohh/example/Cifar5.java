package com.doohh.example;

import java.util.Arrays;

import org.datavec.image.loader.CifarLoader;
import org.deeplearning4j.datasets.iterator.impl.CifarDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.api.IterationListener;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class Cifar5 {
	public static void main(String[] args) {
		final int height = 32;
		final int width = 32;
		int channels = 3;
		int outputNum = CifarLoader.NUM_LABELS;
		int numSamples = 100;
		int batchSize = 128;
		int iterations = 1;
		int seed = 123;
		int listenerFreq = iterations;

		CifarDataSetIterator cifar = new CifarDataSetIterator(batchSize, CifarLoader.NUM_TRAIN_IMAGES, true);

		MultiLayerConfiguration.Builder builder = new NeuralNetConfiguration.Builder().seed(seed).iterations(iterations)
				.gradientNormalization(GradientNormalization.RenormalizeL2PerLayer)
				.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).list()
				.layer(0,
						new ConvolutionLayer.Builder(5, 5).nIn(channels).nOut(6).weightInit(WeightInit.XAVIER)
								.activation("relu").build())
				.layer(1, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX, new int[] { 2, 2 }).build())
				.layer(2,
						new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD).nOut(outputNum)
								.weightInit(WeightInit.XAVIER).activation("softmax").build())
				.backprop(true).pretrain(false).cnnInputSize(height, width, channels);

		MultiLayerNetwork model = new MultiLayerNetwork(builder.build());
		model.init();

		model.setListeners(Arrays.asList((IterationListener) new ScoreIterationListener(listenerFreq)));
		for (int i = 0; i < 10; i++) {
			model.fit(cifar);
			cifar = new CifarDataSetIterator(batchSize, CifarLoader.NUM_TEST_IMAGES, false);
			Evaluation eval = new Evaluation();
			while (cifar.hasNext()) {
				DataSet testDS = cifar.next(batchSize);
				INDArray output = model.output(testDS.getFeatureMatrix());
				eval.eval(testDS.getLabels(), output);
			}
			System.out.println(eval.stats());
		}
	}
}
