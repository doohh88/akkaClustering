package com.doohh.akkaClustering.deploy;

import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.doohh.akkaClustering.dto.AppConf;
import com.doohh.akkaClustering.dto.Command;
import com.doohh.akkaClustering.util.Util;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class SubmitMain {
	private static final Logger log = LoggerFactory.getLogger(SubmitMain.class);

	@Option(name = "--hostIP", usage = "hostIP", aliases = "-h")
	public static String hostIP = "127.0.0.1";
	@Option(name = "--port", usage = "port", aliases = "-p")
	public static String port = "2551";
	@Option(name = "--jar", usage = "jarPath", aliases = "-j")
	public static String jarPath = null;
	@Option(name = "--ss", usage = "classPath", aliases = "-c")
	public static String classPath = null;
	@Option(name = "--maste r", usage = "master", aliases = "-m")
	public static int nMaster = 1;
	@Option(name = "--worker", usage = "worker", aliases = "-w")
	public static int nWorker = 1;

	public static void main(String[] args) {
		// ****************************************************************************
//		args = new String[12];
//		args[0] = "-m";
//		args[1] = "1";
//		args[2] = "-w";
//		args[3] = "4";
//		args[4] = "-j";
//		args[5] = "C:/git/akkaClustering/target/akkaClustering-0.0.1-allinone.jar";
//		args[6] = "-c";
//		args[7] = "com.doohh.akkaClustering.experiments.CifarDistEval";
//		args[8] = "-e";
//		args[9] = "5";
//		args[10] = "-l";
//		args[11] = "10";
//		args[10] = "args3";
		// ****************************************************************************

		String[] appArgs = Util.parseArgs(args, new SubmitMain(), "-c");
		if (jarPath == null || classPath == null) {
			log.error("please input --jar & --class option");
			return;
		}

		Config conf = ConfigFactory.load("deploy");
		final ActorSystem system = ActorSystem.create("deploy", conf);
		final ActorRef submit = system.actorOf(Props.create(Submit.class), "submit");

		AppConf appConf = new AppConf.Builder().hostIP(hostIP).port(port).jarPath(jarPath).classPath(classPath)
				.nMaster(nMaster).nWorker(nWorker).args(appArgs).build();
		log.info("builded appConf: {}", appConf);

		Command cmd = new Command("submit()", appConf);
		submit.tell(cmd, ActorRef.noSender());
		log.info("sended Command to submitActor: {}", cmd);
	}
}
