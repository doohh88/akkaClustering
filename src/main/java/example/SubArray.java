package example;

import org.deeplearning4j.util.MathUtils;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;

public class SubArray {
	public static void main(String[] args) {
		//INDArray a = Nd4j.create(new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, new int[]{1, 10});
		int[] order = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
		
		for(int i = 0 ;i < order.length; i++)
			System.out.print(order[i] + " ");
		System.out.println();
		
		MathUtils.shuffleArray(order, 123);
		for(int i = 0 ;i < order.length; i++)
			System.out.print(order[i] + " ");
		System.out.println();
		// INDArray a = Nd4j.create(new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
		// 11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, new int[]{1, 10});
		// INDArray a = Nd4j.create(new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10},
		// new int[]{1, 10});
		// INDArray z = Nd4j.create(new double[]{5, 4, 3, 2, 1}, new int[]{1,
		// 5});
		// System.out.println(a);
		// a.putScalar(new int[]{1, 5}, z);
		// a.get(NDArrayIndex.interval(0, 1), NDArrayIndex.interval(0,
		// 5)).assign(z);
		// System.out.println(a);
		// System.out.println(a);
		// System.out.println(a.size(0));
		// System.out.println(a.size(1));
		// INDArray b = a.get(NDArrayIndex.interval(0, 1),
		// NDArrayIndex.interval(0, 5));
		// System.out.println("b: " + b);
		// INDArray c = a.get(NDArrayIndex.interval(0, 1),
		// NDArrayIndex.interval(5, 10));
		// System.out.println("c: " + c);
		// b.addi(2);
		// System.out.println(a);
		// INDArray c = a.get(NDArrayIndex.interval(0, 2),
		// NDArrayIndex.interval(0, 5));
		// System.out.println(c);

		// System.out.println(a);
		// System.out.println(Integer.toHexString(System.identityHashCode(a)));
		// System.out.println(a);
		//
		// INDArray b = Nd4j.create(new double[]{5, 4, 3, 2, 1});
		// System.out.println(b);
		//
		// a.put(0, b);

		// INDArray b = a.get(NDArrayIndex.interval(0, 5));
		// System.out.println(b);

		// System.out.println(Integer.toHexString(System.identityHashCode(b)));
		// System.out.println(a == b);
		//
		// INDArray c = a.slice(5);
		// System.out.println(c);
	}
	
	
}
