package com.doohh.akkaClustering.worker;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import com.doohh.akkaClustering.dto.AppConf;
import com.doohh.akkaClustering.dto.Command;
import com.doohh.akkaClustering.util.Util;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.util.Timeout;
import example.DistLenet;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;

public class Task extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	private ActorSelection master = null;
	private ActorRef comm = null;
	private final ExecutionContext ec;
	private Timeout timeout = new Timeout(Duration.create(10, "seconds"));

	public Task() {
		ec = context().system().dispatcher();
	}

	@Override
	public void onReceive(Object message) throws Throwable {
		if (message instanceof Command) {
			Command cmd = (Command) message;
			if (cmd.getCommand().equals("runApp()")) {
				AppConf appConf = (AppConf) cmd.getData();
				log.info("get appConf from worker : {}", appConf);
				master = getContext().actorSelection(getSender().path().address() + "/user/master");
				generateComm(appConf);
				writeTaskProp(appConf);

				// *******************
				// running application
				// runApp(appConf);
				// new LoadTaskPropMain().main(null);
				new DistLenet().main(null);
				// new HashTableMain().main(null);
				// new PushGradPullParam().main(null);
				// *******************

				log.info("send msg(complet task) to {}", getSender());
				master.tell(new Command().setCommand("finishApp()").setData(appConf.getRouterInfo()), getSelf());
			}
		}

		else if (message instanceof String) {
			log.info("received msg = {}", (String) message);
		} else {
			log.info("received unhandled msg");
			unhandled(message);
		}
	}

	void runApp(AppConf appConf) {
		log.info("running application: {}", appConf);
		File jarFile = appConf.getJarFile();
		String classPath = appConf.getClassPath();
		String[] args = appConf.getArgs();

		try {
			URL classURL = new URL("jar:" + jarFile.toURI().toURL() + "!/");
			URLClassLoader classLoader = new URLClassLoader(new URL[] { classURL });
			Class<?> clazz = classLoader.loadClass(classPath);
			Method method = clazz.getMethod("main", String[].class);
			method.invoke(null, (Object) args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void writeTaskProp(AppConf appConf) {
		String fileName = Util.getHomeDir() + "/conf/task_" + appConf.getRole() + appConf.getRoleIdx() + ".properties";
		log.info("task.properties's location: {}", fileName);
		String content = "role=" + appConf.getRole() + "\nroleIdx=" + appConf.getRoleIdx();
		content += "\nparamNodes=";
		for (String addr : appConf.getRouterInfo().getParamAddr()) {
			content += addr + "/task/pcomm,";
		}
		content = content.substring(0, content.length() - 1);
		content += "\nslaveNodes=";
		for (String addr : appConf.getRouterInfo().getSlaveAddr()) {
			content += addr + "/task/scomm,";
		}
		content = content.substring(0, content.length() - 1);
		Util.write(fileName, content);
	}

	void generateComm(AppConf appConf) {
		String role = appConf.getRole();
		if (role.equals("param")) {
			this.comm = context().actorOf(Props.create(PComm.class), "pcomm");
		} else {
			this.comm = context().actorOf(Props.create(SComm.class), "scomm");
		}
	}
}
