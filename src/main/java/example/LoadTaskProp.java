package example;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import com.doohh.akkaClustering.dto.RouterInfo;
import com.doohh.akkaClustering.util.PropFactory;
import com.doohh.akkaClustering.util.Util;
import com.doohh.akkaClustering.worker.WorkerMain;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;

public class LoadTaskProp {
	private String role;
	private String roleIdx;
	private ActorSelection task;
	private RouterInfo routerInfo;
	private Properties props;

	private void loadTaskProp() {
		// read confFile
		String path = Util.getHomeDir() + "/conf";
		File[] fileList = Util.getFileList(path);
		ArrayList<File> confFiles = new ArrayList<File>();
		for (File file : fileList) {
			if (file.getName().contains("task_")) {
				confFiles.add(file);
			}
		}
		File confFile = confFiles.get(WorkerMain.n++); // if window
		String confFileName = confFile.getName();
		System.out.println(confFile);
		props = PropFactory.getInstance(confFileName).getProperties();
		this.role = props.getProperty("role");
		this.roleIdx = props.getProperty("roleIdx");
		this.task = WorkerMain.actorSystem.actorSelection("/user/worker/task");
		setNetforProc();

		// remove confFile after reading it
		confFile.delete();
	}

	private void setNetforProc() {
		this.routerInfo = new RouterInfo();
		String paramAddrs = props.getProperty("paramNodes");
		this.routerInfo.setParamAddr(new ArrayList<String>(Arrays.asList(new String(paramAddrs).split(","))));
		String slaveAddrs = props.getProperty("slaveNodes");
		this.routerInfo.setSlaveAddr(new ArrayList<String>(Arrays.asList(new String(slaveAddrs).split(","))));
		this.routerInfo.setActorSelection();
	}

	public static void main(String[] args) {
		LoadTaskProp l = new LoadTaskProp();
		l.loadTaskProp();

		if (l.role.equals("param")) {
			l.routerInfo.getSlaveAgents().get(0).tell("hello, i'm parameter server", ActorRef.noSender());
		}
	}

}
