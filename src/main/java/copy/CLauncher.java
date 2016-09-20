package com.doohh.akkaClustering.deplo;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import akka.actor.ActorRef;
import akka.actor.Address;
import akka.actor.UntypedActorWithStash;
import akka.dispatch.OnComplete;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Procedure;
import akka.pattern.Patterns;
import akka.routing.BroadcastGroup;
import akka.util.Timeout;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class Launcher extends UntypedActorWithStash {
	// Logger log = LoggerFactory.getLogger(Launcher.class);
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	private Timeout timeout = new Timeout(Duration.create(5, "seconds"));
	private final ExecutionContext ec;
	HashMap<Address, ActorRef> workers = new HashMap<Address, ActorRef>();
	UserAppConf userAppConf;
	ClassLoader userClassLoader;
	ActorRef router;

	public Launcher() {
		getContext().become(init);
		ec = context().system().dispatcher();
	}

	@Override
	public void onReceive(Object message) throws Throwable {

	}

	Procedure<Object> init = new Procedure<Object>() {
		public void apply(Object message) {
			if (message instanceof UserAppConf) {
				userAppConf = (UserAppConf) message;
				Future<Object> f = Patterns.ask(getSender(), "getWorkers", timeout);
				f.onSuccess(new SaySuccess<Object>(), ec);
				f.onComplete(new SayComplete<Object>(), ec);
				f.onFailure(new SayFailure<Object>(), ec);
				getContext().become(start);
			} else {
				stash();
			}

		}
	};

	Procedure<Object> start = new Procedure<Object>() {
		public void apply(Object message) {
			if (message instanceof UserAppConf) {

				getContext().become(done);
			} else {
				stash();
			}
		}
	};

	Procedure<Object> done = new Procedure<Object>() {
		public void apply(Object message) {
			if (message instanceof UserAppConf) {

				getContext().become(init);
			} else {
				stash();
			}
		}
	};

	public final class SaySuccess<T> extends OnSuccess<T> {
		@Override
		public final void onSuccess(T result) {
			log.info("Succeeded with " + result);
			workers = (HashMap<Address, ActorRef>) result;
			router = ia(workers);
			router.tell(userAppConf, getSelf());
		}
	}

	public final class SayFailure<T> extends OnFailure {
		@Override
		public final void onFailure(Throwable t) {
			log.info("Failed with " + t);
		}
	}

	public final class SayComplete<T> extends OnComplete<T> {
		@Override
		public final void onComplete(Throwable t, T result) {
			log.info("Completed.");
		}
	}

	public ActorRef ia(HashMap<Address, ActorRef> workers) {
		List<String> routeePaths = new ArrayList<String>();
		for (ActorRef p : workers.values()) {
			routeePaths.add(p.path().toString());
		}
		ActorRef router = getContext().actorOf(new BroadcastGroup(routeePaths).props(), "router");
		return router;
	}

	public ClassLoader classLoad() {
		File file = new File(userAppConf.getPackagePath());
		try {
			URL url = file.toURL();
			URL[] urls = new URL[] { url };
			return new URLClassLoader(urls);
			// userClassLoader = new URLClassLoader(urls);
			// invokeClass(userAppConf.getMainClass(), new String[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void invokeClass(ClassLoader userClassLoader, String name, String[] args)
			throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
		Class clazz = userClassLoader.loadClass(name);
		Method method = clazz.getMethod("main", new Class[] { args.getClass() });
		method.setAccessible(true);
		int mods = method.getModifiers();
		if (method.getReturnType() != void.class || !Modifier.isStatic(mods) || !Modifier.isPublic(mods)) {
			throw new NoSuchMethodException("main");
		}
		try {
			method.invoke(null, new Object[] { args });
		} catch (IllegalAccessException e) {
			// This should not happen, as we have disabled access checks
		}
	}

}

// userClassLoader = classLoad();