package com.doohh.akkaClustering.dto;

import java.io.Serializable;
import java.util.ArrayList;

import akka.actor.ActorRef;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Barrier implements Serializable {
	int curNWait;
	int barrierNum;
	String methodName;
	ArrayList<ActorRef> returnList = new ArrayList<ActorRef>();
	
	public Barrier(int barrierNum, String methodName) {
		this.barrierNum = barrierNum;
		this.methodName = methodName;
	}
	
	public boolean count(){
		curNWait++;
		if (curNWait == barrierNum) {
			return true;
		} else
			return false;
	}
}
