package com.doohh.akkaClustering.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.doohh.akkaClustering.worker.WorkerMain;

import akka.actor.ActorSelection;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class AppNetInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private List<String> paramAddr = null;
	private List<String> slaveAddr = null;
	private ArrayList<ActorSelection> paramNodes = null;
	private ArrayList<ActorSelection> slaveNodes = null;

	public AppNetInfo() {
		paramAddr = new ArrayList<String>();
		slaveAddr = new ArrayList<String>();
	}

	public void setActorSelection() {
		for (String addr : paramAddr) {
			paramNodes.add(WorkerMain.actorSystem.actorSelection(addr));
		}
		for (String addr : slaveAddr) {
			slaveNodes.add(WorkerMain.actorSystem.actorSelection(addr));
		}
	}
}
