package example;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;

public class SubArray {
	public static void main(String[] args) {
		INDArray a = Nd4j.create(new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, new int[]{2, 10});
		System.out.println(a);
		INDArray b = a.get(NDArrayIndex.interval(0, 1), NDArrayIndex.interval(0, 5));		
		System.out.println(b);
		b.addi(2);
		System.out.println(a);
//		INDArray c = a.get(NDArrayIndex.interval(0, 2), NDArrayIndex.interval(0, 5));		
//		System.out.println(c);
		
		//System.out.println(a);
//		System.out.println(Integer.toHexString(System.identityHashCode(a)));
//		System.out.println(a);
//		
//		INDArray b = Nd4j.create(new double[]{5, 4, 3, 2, 1});
//		System.out.println(b);
//		
		//a.put(0, b);
		
		//INDArray b = a.get(NDArrayIndex.interval(0, 5));
		//System.out.println(b);

		
//		System.out.println(Integer.toHexString(System.identityHashCode(b)));
//		System.out.println(a == b);
//		
//		INDArray c = a.slice(5);
//		System.out.println(c);
	}
}
