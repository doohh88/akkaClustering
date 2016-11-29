package com.doohh.akkaClustering.worker;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import com.doohh.akkaClustering.dto.AppConf;
import com.doohh.akkaClustering.dto.Command;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.ExecutionContext;

public class Task extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	private ActorRef comm = null;
	private AppConf appConf = null;
	private final ExecutionContext ec;

	public Task() {
		ec = context().system().dispatcher();
	}

	@Override
	public void onReceive(Object message) throws Throwable {
		if (message instanceof Command) {
			Command cmd = (Command) message;
			if (cmd.getCommand().equals("runApp()")) {
				appConf = (AppConf) cmd.getData();
				log.info("get appConf from worker : {}", appConf);
				generateComm(appConf);

				// *******************
				// running application
				runApp(appConf);
				// new LenetDistEx().main(appConf, appConf.getArgs());
				// new LenetSyncDistEx().main(appConf, appConf.getArgs());
				//new CifarDistEx().main(appConf, appConf.getArgs());				
				//new CifarDistSyncEx().main(appConf, appConf.getArgs());
				// new DistTest().main(appConf, appConf.getArgs());
				// *******************
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
			Method method = clazz.getMethod("main", AppConf.class, String[].class);
			method.invoke(null, appConf, (Object) args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void generateComm(AppConf appConf) {
		String role = appConf.getRole();
		if (role.equals("param")) {
			this.comm = context().actorOf(Props.create(PComm.class), "pcomm" + appConf.getRoleIdx());
		}
	}
}
