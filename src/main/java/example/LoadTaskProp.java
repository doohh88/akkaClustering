package example;

import java.io.File;
import java.util.ArrayList;
import java.util.Properties;

import com.doohh.akkaClustering.util.AppNetInfo;
import com.doohh.akkaClustering.util.PropFactory;
import com.doohh.akkaClustering.util.Util;
import com.doohh.akkaClustering.worker.WorkerMain;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;

public class LoadTaskProp {
	private String role;
	private String roleIdx;
	private ActorSelection worker;
	private AppNetInfo appNetInfo;
	private Properties props;
	
	private void loadTaskProp() {
		String path = Util.getHomeDir() + "/conf";
		File[] fileList = Util.getFileList(path);
		ArrayList<File> confFiles = new ArrayList<File>();
		for(File file : fileList){
			if (file.getName().contains("task_")) {
				confFiles.add(file);
			}
		}
		File confFile = confFiles.get(WorkerMain.n++);
		String confFileName = confFile.getName();
		System.out.println(confFile);
		props = PropFactory.getInstance(confFileName).getProperties();
		this.role = props.getProperty("role");
		this.roleIdx = props.getProperty("roleIdx");
		this.worker = WorkerMain.actorSystem.actorSelection("/user/worker");
		this.worker.tell("hello i'm application", ActorRef.noSender());
		setAppNetInfo();
		confFile.delete();
	}

	private void setAppNetInfo() {
		this.appNetInfo = new AppNetInfo();
		String paramAddrs = props.getProperty("paramNodes");
		this.appNetInfo.getParamAddr().toArray(new String(paramAddrs).split(","));
		String slaveAddrs = props.getProperty("slaveNodes");
		this.appNetInfo.getParamAddr().toArray(new String(slaveAddrs).split(","));
		this.appNetInfo.setActorSelection();
	}
	
	public static void main(String[] args) {
		new LoadTaskProp().loadTaskProp();
	}

}
