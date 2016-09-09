package com.doohh.akkaClustering.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.doohh.akkaClustering.master.Master;
import com.doohh.akkaClustering.util.PropFactory;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorSystem;
import akka.actor.Props;

public class WorkerMain {
	public static String hostIp;
	public static String port;
	public static String systemName = "deepDist";

	public static void main(String[] args) {
		Logger log = LoggerFactory.getLogger(WorkerMain.class);
		String seedNodes = PropFactory.getInstance().getSeedConf("worker");
		String role = "[worker]";
		hostIp = args.length > 0 ? args[0] : "127.0.0.1";
		port = "0";
		
		log.info("Starting distDepp worker");

		Config conf = ConfigFactory.parseString("akka.remote.netty.tcp.hostname=" + hostIp)
				.withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port))
				.withFallback(ConfigFactory.parseString("akka.cluster.seed-nodes=" + seedNodes))
				.withFallback(ConfigFactory.parseString("akka.cluster.roles=" + role))
				.withFallback(ConfigFactory.load("application"));
		
		ActorSystem actorSystem = ActorSystem.create(systemName, conf);
		actorSystem.actorOf(Props.create(Worker.class), "worker");
	}
}
