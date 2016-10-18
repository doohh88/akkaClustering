package example;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
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
	private FileLock lock = null;
	private FileChannel channel = null;

	private void loadTaskProp() {
		// read confFile
		File confFile = null;
		String path = Util.getHomeDir() + "/conf";
		File[] fileList = Util.getFileList(path);
		ArrayList<File> confFiles = new ArrayList<File>();
		for (File file : fileList) {
			if (file.getName().contains("task_")) {
				try {
					if (checkFile(file)) {
						confFile = file;
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		try {
			Thread.sleep(1000);
			lock.release();
			channel.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.role = props.getProperty("role");
		this.roleIdx = props.getProperty("roleIdx");
		this.task = WorkerMain.actorSystem.actorSelection("/user/worker/task");
		setNetforProc();

		// remove confFile after reading it'
		try {
			confFile.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setNetforProc() {
		this.routerInfo = new RouterInfo();
		String paramAddrs = props.getProperty("paramNodes");
		this.routerInfo.setParamAddr(new ArrayList<String>(Arrays.asList(new String(paramAddrs).split(","))));
		String slaveAddrs = props.getProperty("slaveNodes");
		this.routerInfo.setSlaveAddr(new ArrayList<String>(Arrays.asList(new String(slaveAddrs).split(","))));
		this.routerInfo.setActorSelection();
	}

	private boolean checkFile(File file) {
		try {
			props = PropFactory.getInstance(file.getName()).getProperties();
			if (props == null) {
				return false;
			} else {
				this.channel = new RandomAccessFile(file, "rw").getChannel();
				this.lock = this.channel.lock();
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	public static void main(String[] args) {
		LoadTaskProp l = new LoadTaskProp();
		l.loadTaskProp();

		System.out.println("hello world");
		if (l.role.equals("param")) {
			l.routerInfo.getSlaveAgents().get(0).tell("hello, i'm parameter server", ActorRef.noSender());
		}

	}

}
