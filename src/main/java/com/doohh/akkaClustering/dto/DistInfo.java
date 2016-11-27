package com.doohh.akkaClustering.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import com.doohh.akkaClustering.nn.DistMnistDataFetcher;
import com.doohh.akkaClustering.worker.WorkerMain;

import akka.actor.ActorSelection;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class DistInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Properties props;
	private String role;
	private int roleIdx;
	private RouterInfo routerInfo;
	private ActorSelection comm;
	private ActorSelection controller;

	public void init(AppConf appConf) {
		this.role = appConf.getRole();
		this.roleIdx = appConf.getRoleIdx();
		if (this.role.equals("param"))
			this.comm = WorkerMain.actorSystem.actorSelection("/user/worker/task/pcomm" + this.roleIdx);
		else
			this.comm = WorkerMain.actorSystem.actorSelection("/user/worker/task/scomm" + this.roleIdx);
		this.routerInfo = appConf.getRouterInfo();
		this.routerInfo.setActorSelection();
		this.controller = WorkerMain.actorSystem.actorSelection(this.routerInfo.getParamAddr().get(0) + "/controller");
	}

	public void setParamRange(int numParmas) {
		// set parameter range depending the idx of role
		this.routerInfo.setRange(numParmas);
	}

	public int getNumExamples(String fetcherName) {
		int totalSize = 0;
		if (fetcherName.equals("DistMnistDataFetcher")) {
			totalSize = DistMnistDataFetcher.NUM_EXAMPLES;
		}
		int numExamples = totalSize / (routerInfo.getNProcServer()) * (roleIdx + 1);
		return numExamples;
	}
}

/*
 * private void setNetforProc() { this.routerInfo = new RouterInfo(); String
 * paramAddrs = props.getProperty("paramNodes");
 * this.routerInfo.setParamAddr(new ArrayList<String>(Arrays.asList(new
 * String(paramAddrs).split(",")))); String slaveAddrs =
 * props.getProperty("slaveNodes"); this.routerInfo.setSlaveAddr(new
 * ArrayList<String>(Arrays.asList(new String(slaveAddrs).split(","))));
 * this.routerInfo.setActorSelection(); }
 */
