package com.doohh.akkaClustering.worker;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import com.doohh.akkaClustering.deploy.AppConf;

import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class Task extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	private Timeout timeout = new Timeout(Duration.create(10, "seconds"));
	private ActorSelection launcher = null;
	
	@Override
	public void onReceive(Object message) throws Throwable {
		if (message instanceof AppConf) {
			AppConf appConf = (AppConf) message;
			log.info("get appConf from worker : {}", appConf);
			launcher = getContext().actorSelection(getSender().path().address() + "/user/master/launcher");
			runApp(appConf);
			log.info("send msg(complet task) to {}", getSender());
			Future<Object> future = Patterns.ask(launcher, "finish()", timeout);
			String result = (String) Await.result(future, timeout.duration());
			context().stop(getSelf());
			log.info("stop the task");
		}
		
		else {
			log.info("receive unhandled msg");
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
			URLClassLoader classLoader = new URLClassLoader(new URL[] {classURL});
			Class<?> clazz = classLoader.loadClass(classPath);			
			Method method = clazz.getMethod("main", String[].class);
			method.invoke(null, (Object)args);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
