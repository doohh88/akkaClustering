package com.doohh.akkaClustering.worker;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import com.doohh.akkaClustering.dto.AppConf;
import com.doohh.akkaClustering.util.Util;

import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.dispatch.OnComplete;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class Task extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	private Timeout timeout = new Timeout(Duration.create(10, "seconds"));
	private ActorSelection master = null;
	private final ExecutionContext ec;

	public Task() {
		ec = context().system().dispatcher();
	}

	@Override
	public void onReceive(Object message) throws Throwable {
		if (message instanceof AppConf) {
			AppConf appConf = (AppConf) message;
			log.info("get appConf from worker : {}", appConf);
			master = getContext().actorSelection(getSender().path().address() + "/user/master");
			writeTaskProp(appConf);
			
			// *******************
			// running application
			runApp(appConf);
			// *******************
			
			log.info("send msg(complet task) to {}", getSender());
			Future<Object> future = Patterns.ask(master, "finishApp()", timeout);
			future.onSuccess(new OnSuccess<Object>() {
				@Override
				public void onSuccess(Object result) throws Throwable {
					log.info("Succeeded sending with: " + result);
				}
			}, ec);

			future.onFailure(new OnFailure() {
				@Override
				public void onFailure(Throwable t) throws Throwable {
					log.info("Failed to send with: " + t);
				}
			}, ec);

			future.onComplete(new OnComplete<Object>() {
				@Override
				public void onComplete(Throwable t, Object result) throws Throwable {
					log.info("Completed.");
					context().stop(getSelf());
					log.info("stop the task");
				}
			}, ec);
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
			content += addr + ",";
		}
		content = content.substring(0, content.length() - 1);
		content += "\nslaveNodes=";
		for (String addr : appConf.getRouterInfo().getSlaveAddr()) {
			content += addr + ",";
		}
		content = content.substring(0, content.length() - 1);
		Util.write(fileName, content);
	}
}
