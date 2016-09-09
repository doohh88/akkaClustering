package com.doohh.example;

import org.datavec.image.loader.CifarLoader;
import org.deeplearning4j.datasets.iterator.impl.CifarDataSetIterator;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

public class CifarDist {
	public static void main(String[] args) {
		int batchSize = 50;
		//int numExamples = CifarLoader.NUM_TRAIN_IMAGES;
		int numExamples = 10;
		int numNode = 2;
		DataSetIterator mnistTrain = new CifarDataSetIterator(batchSize, numExamples, new int[]{32, 32, 3}, true);
		mnistTrain
		
		
        //DataSetIterator mnistTest = new CifarDataSetIterator(batchSize, CifarLoader.NUM_TEST_IMAGES, new int[]{32, 32, 3}, false);
	}
}
