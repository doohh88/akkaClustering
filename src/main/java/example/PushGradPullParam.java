package example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import org.deeplearning4j.optimize.api.IterationListener;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import com.doohh.akkaClustering.dto.Command;
import com.doohh.akkaClustering.dto.RouterInfo;
import com.doohh.akkaClustering.worker.WorkerMain;
import com.doohh.nn.LoadTaskProp;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;

public class PushGradPullParam {
	private Collection<IterationListener> listeners = new ArrayList<>();
	private Properties props;
	private String role = null;
	private String roleIdx = null;
	private RouterInfo routerInfo = null;
	private ActorSelection agent = null;
	private Timeout timeout = new Timeout(scala.concurrent.duration.Duration.create(10, "seconds"));

	private void proc() {
		this.props = (new LoadTaskProp()).loadTaskProp();
		this.role = props.getProperty("role");
		this.roleIdx = props.getProperty("roleIdx");
		this.agent = WorkerMain.actorSystem.actorSelection("/user/worker/task/agent");
		setNetforProc();

		// test
		pushGradPullParam();
	}

	private void setNetforProc() {
		this.routerInfo = new RouterInfo();
		String paramAddrs = props.getProperty("paramNodes");
		this.routerInfo.setParamAddr(new ArrayList<String>(Arrays.asList(new String(paramAddrs).split(","))));
		String slaveAddrs = props.getProperty("slaveNodes");
		this.routerInfo.setSlaveAddr(new ArrayList<String>(Arrays.asList(new String(slaveAddrs).split(","))));
		this.routerInfo.setActorSelection();
	}

	private void pushGradPullParam() {
		if (this.role.equals("slave")) {
			System.out.println("hello i'm slave");
			for (ActorSelection as : routerInfo.getParamComms()) {
				try {
					INDArray grad = Nd4j.create(new float[] { 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 },
							new int[] { 1, 12 });

					Future<Object> future = Patterns.ask(as, new Command().setCommand("pushGradient()").setData(grad),
							timeout);
					INDArray param = (INDArray) Await.result(future, timeout.duration());
					System.out.println("param: " + param);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) {
		new PushGradPullParam().proc();
	}
}