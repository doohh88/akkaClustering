package example;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.nd4j.Nd4jSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
public class SubArray {
	public static void main(String[] args) {
		Kryo kryo = new Kryo();
//		kryo.register(Nd4j.getBackend().getNDArrayClass(), new Nd4jSerializer());
//        kryo.register(Nd4j.getBackend().getComplexNDArrayClass(), new Nd4jSerializer());
//		kryo.register(INDArray.class, new Nd4jSerializer());
//		kryo.register(org.nd4j.linalg.cpu.nativecpu.NDArray.class, new Nd4jSerializer());

		
		INDArray a = Nd4j.create(new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, new int[]{1, 20});
		System.out.println(a.size(1));
		System.out.println(a.length());
		
		Nd4jSerializer se = new Nd4jSerializer();
	    Output output = new Output(150);
		se.write(kryo, output, a);
	    System.out.println(output.toBytes().length);
		byte[] b = output.toBytes();
		
	    Input input = new Input(b);
	    INDArray rst = (INDArray) se.read(kryo, input, INDArray.class);
	    System.err.println(rst);
	    
//		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
//	    Input ki = new Input(bis);
//	    se.read(kryo, ki, INDArray.class);
	    
	    //kryo.writeObject(ko, a);
	    
	    
	   // INDArray des = (INDArray)kryo.readObject(ki, INDArray.class);
	   // System.out.println(des);
//		INDArray b = a;
		//System.out.println(b);
//		System.out.println(a.size(1)); //20
//		INDArray f = a.get(NDArrayIndex.interval(0, 1), NDArrayIndex.interval(0, 20));
//		//INDArray b = a.get(NDArrayIndex.interval(0, 1), NDArrayIndex.interval(10, 20));
//		System.out.println(f);
//		//System.out.println(b);
		
		//int[] order = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20};

		
		
//		for(int i = 0 ;i < order.length; i++)
//			System.out.print(order[i] + " ");
//		System.out.println();
//		
//		MathUtils.shuffleArray(order, 123);
//		for(int i = 0 ;i < order.length; i++)
//			System.out.print(order[i] + " ");
//		System.out.println();
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
