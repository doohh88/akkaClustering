package com.doohh.akkaClustering;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorSystem;

public class Master {
	public static String hostIp;
	public static String port;
	public static String systemName = "deepDist";

	public static void main(String[] args) {
		Logger log = LoggerFactory.getLogger(Master.class);
		
    	hostIp = args.length > 0 ? args[0] : "127.0.0.1";
		port = "2551";
		
		String seedNodes = PropFactory.getInstance().getSeedConf("master");
		log.info("Starting distDepp Master");

		Config conf = ConfigFactory.parseString("akka.remote.netty.tcp.hostname=" + hostIp)
				.withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port))
				.withFallback(ConfigFactory.parseString("akka.cluster.seed-nodes=" + seedNodes))
				.withFallback(ConfigFactory.load("application"));
		
		log.info("hostname : " + conf.getString("akka.remote.netty.tcp.hostname"));
		log.info("hostport : " + conf.getString("akka.remote.netty.tcp.port"));
		log.info("seed-nodes : " + conf.getList("akka.cluster.seed-nodes"));
		
        ActorSystem actorSystem = ActorSystem.create(systemName, conf);
	}
}