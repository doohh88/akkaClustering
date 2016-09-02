package com.doohh.akkaClustering.deploy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class SubmitMain {
	Logger log = LoggerFactory.getLogger(SubmitMain.class);

	UserAppConf userAppConf;
	ClassLoader userClassLoader;
	
	public static void main(String[] args) {
		SubmitCli submitCli = new SubmitCli(args);
	    Config conf = ConfigFactory.load("deploy");
	    final ActorSystem system = ActorSystem.create("deploy", conf);
	    final ActorRef actor  = system.actorOf(Props.create(Submit.class), "submit");
	    actor.tell(submitCli.getUserAppConf(), ActorRef.noSender());
	}
}
