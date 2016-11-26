package example;

import java.io.IOException;

import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

public class LenetDatasetExample {
	public static void main(String[] args) {
		int batchSize = 64;
		int numExamples = 30000;
		try {
			DataSetIterator mnistTrain = new MnistDataSetIterator(batchSize, numExamples, false, true, true, 12345);
			System.out.println(mnistTrain.next(10).getLabels());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
