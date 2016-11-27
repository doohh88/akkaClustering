package com.doohh.akkaClustering.util;

import org.nd4j.Nd4jSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Nd4jSerialization {
	private Kryo kryo;
	private Nd4jSerializer nd4jSerializer; 
	
	public Nd4jSerialization() {
		this.kryo = new Kryo();
		this.nd4jSerializer = new Nd4jSerializer();
	}
	
	public byte[] serialize(INDArray iNDArray){
		Output output = new Output(iNDArray.length()*8);
		nd4jSerializer.write(kryo, output, iNDArray);
		return output.toBytes();
	}
	
	public byte[] serialize(int bufferSize, INDArray iNDArray){
		Output output = new Output(bufferSize);
		nd4jSerializer.write(kryo, output, iNDArray);
		return output.toBytes();
	}
	
	public INDArray deserialize(byte[] INDArray_byte){
		Input input = new Input(INDArray_byte);
		return (INDArray) nd4jSerializer.read(kryo, input, INDArray.class);
	}
}
