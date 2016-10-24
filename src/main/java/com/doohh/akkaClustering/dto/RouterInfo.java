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
	private ActorRef router;
	private ArrayList<String> paramAddr = null;
	private ArrayList<String> slaveAddr = null;
	private ArrayList<ActorSelection> paramAgents = null;
	private ArrayList<ActorSelection> slaveAgents = null;

	public RouterInfo() {
		paramAddr = new ArrayList<String>();
		slaveAddr = new ArrayList<String>();
	}

	public void setActorSelection() {
		paramAgents = new ArrayList<ActorSelection>();
		slaveAgents = new ArrayList<ActorSelection>();
		for (String addr : paramAddr) {
			paramAgents.add(WorkerMain.actorSystem.actorSelection(addr));
		}
		for (String addr : slaveAddr) {
			slaveAgents.add(WorkerMain.actorSystem.actorSelection(addr));
		}
	}
}
