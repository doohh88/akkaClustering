package com.doohh.akkaClustering.master;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.doohh.akkaClustering.util.PropFactory;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorSystem;
import akka.actor.Props;

public class MasterMain {
	public static String hostIp;
	public static String port;
	public static String systemName = "deepDist";

	public static void main(String[] args) {
		Logger log = LoggerFactory.getLogger(MasterMain.class);

		hostIp = args.length > 0 ? args[0] : "127.0.0.1";
		port = "2551";

		String seedNodes = PropFactory.getInstance().getSeedConf("master");
		String role = "[master]";
		log.info("Starting distDepp Master");

		Config conf = ConfigFactory.parseString("akka.remote.netty.tcp.hostname=" + hostIp)
				.withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port))
				.withFallback(ConfigFactory.parseString("akka.cluster.seed-nodes=" + seedNodes))
				.withFallback(ConfigFactory.parseString("akka.cluster.roles=" + role))
				.withFallback(ConfigFactory.load("application"));

//		log.info("hostname : " + conf.getString("akka.remote.netty.tcp.hostname"));
//		log.info("hostport : " + conf.getString("akka.remote.netty.tcp.port"));
//		log.info("seed-nodes : " + conf.getList("akka.cluster.seed-nodes"));
//		log.info("roles : " + conf.getList("akka.cluster.roles"));

		ActorSystem actorSystem = ActorSystem.create(systemName, conf);
		actorSystem.actorOf(Props.create(Master.class), "master");

	}
}