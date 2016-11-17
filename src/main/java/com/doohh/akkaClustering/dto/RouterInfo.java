package com.doohh.akkaClustering.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.doohh.akkaClustering.worker.WorkerMain;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RouterInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int nNodes;
	private int nParamServer;
	private ActorRef router;
	private ArrayList<Range> paramRange = null;
	private ArrayList<String> paramAddr = null;
	private ArrayList<String> slaveAddr = null;
	private ArrayList<ActorSelection> paramComms = null;
	private ArrayList<ActorSelection> slaveComms = null;

	public RouterInfo() {
		paramAddr = new ArrayList<String>();
		slaveAddr = new ArrayList<String>();
	}

	public void setActorSelection() {
		paramComms = new ArrayList<ActorSelection>();
		slaveComms = new ArrayList<ActorSelection>();
		for (String addr : paramAddr) {
			paramComms.add(WorkerMain.actorSystem.actorSelection(addr));
		}
		for (String addr : slaveAddr) {
			slaveComms.add(WorkerMain.actorSystem.actorSelection(addr));
		}
	}

	public void setRange(int paramSize) {
		this.nNodes = paramAddr.size() + slaveAddr.size();
		this.nParamServer = paramAddr.size();
		paramRange = new ArrayList<Range>();
		int q = paramSize / nParamServer;
		int r = paramSize % nParamServer;
		int start, end;
		for (int i = 0; i < paramAddr.size(); i++) {
			start = q * i;
			if (i == nParamServer - 1)
				end = paramSize;
			else
				end = start + q;
			paramRange.add(new Range(start, end));
		}
		// System.out.println("paramSize: " + paramSize);
		// System.out.println("range: " + paramRange);
	}

	@Data
	@ToString
	public class Range {
		int start;
		int end;

		public Range(int start, int end) {
			this.start = start;
			this.end = end;
		}
	}
}
