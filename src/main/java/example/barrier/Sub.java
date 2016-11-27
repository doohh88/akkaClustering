package example.barrier;

import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.doohh.akkaClustering.util.PropFactory;
import com.doohh.akkaClustering.util.Util;
import com.doohh.akkaClustering.worker.WorkerMain;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class Sub {
	private static final Logger log = LoggerFactory.getLogger(Sub.class);

	@Option(name = "--hostIP", usage = "hostIP", aliases = "-h")
	public static String hostIP = "127.0.0.1";
	@Option(name = "--port", usage = "port", aliases = "-p")
	public static String port = "0";
	@Option(name = "--n", usage = "n", aliases = "-n")
	public static int n = 0;
	public static String systemName = "deepDist";
	public static ActorSystem actorSystem = null;
	

	public static void main(String[] args) {
		String seedNodes = PropFactory.getInstance("config.properties").getSeedConf("worker");
		String role = "[worker]";

		Util.parseArgs(args, new Sub());

		log.info("Starting distDepp worker");
		Config conf = ConfigFactory.parseString("akka.remote.netty.tcp.hostname=" + hostIP)
				.withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port))
				.withFallback(ConfigFactory.parseString("akka.cluster.seed-nodes=" + seedNodes))
				.withFallback(ConfigFactory.parseString("akka.cluster.roles=" + role))
				.withFallback(ConfigFactory.load("application"));

		actorSystem = ActorSystem.create(systemName, conf);
		ActorRef subActor = actorSystem.actorOf(Props.create(SubActor.class, n), "subActor");
		subActor.tell("run()", ActorRef.noSender());

	}
}
