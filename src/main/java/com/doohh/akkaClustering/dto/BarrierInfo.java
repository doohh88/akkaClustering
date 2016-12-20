package com.doohh.akkaClustering.dto;

import java.io.Serializable;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class BarrierInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	int barrierNum;
	boolean hasNext;

	public BarrierInfo(int barrierNum, boolean hasNext) {
		this.barrierNum = barrierNum;
		this.hasNext = hasNext;
	}
}


//int iteration;	
//DistInfo distInfo;
//public BarrierInfo(DistInfo distInfo, int barrierNum, int iteration) {
//	//this.distInfo = distInfo;
//	this.barrierNum = barrierNum;
//	this.iteration = iteration;
//}
