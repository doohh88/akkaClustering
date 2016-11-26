package example.barrier;

import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.doohh.akkaClustering.master.MasterMain;
import com.doohh.akkaClustering.util.PropFactory;
import com.doohh.akkaClustering.util.Util;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class Main {
	private static final Logger log = LoggerFactory.getLogger(Main.class);

	@Option(name = "--hostIP", usage = "hostIP", aliases = "-h")
	public static String hostIP = "127.0.0.1";
	@Option(name = "--port", usage = "port", aliases = "-p")
	public static String port = "2551";
	public static String systemName = "deepDist";

	public static void main(String[] args) {
		Util.parseArgs(args, new Main());

		String seedNodes = PropFactory.getInstance("config.properties").getSeedConf("master");
		String role = "[master]";

		log.info("Starting distDepp Master");
		Config conf = ConfigFactory.parseString("akka.remote.netty.tcp.hostname=" + hostIP)
				.withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port))
				.withFallback(ConfigFactory.parseString("akka.cluster.seed-nodes=" + seedNodes))
				.withFallback(ConfigFactory.parseString("akka.cluster.roles=" + role))
				.withFallback(ConfigFactory.load("application"));

		ActorSystem actorSystem = ActorSystem.create(systemName, conf);
		ActorRef mainActor = actorSystem.actorOf(Props.create(MainActor.class, 3), "mainActor");
	}
}
