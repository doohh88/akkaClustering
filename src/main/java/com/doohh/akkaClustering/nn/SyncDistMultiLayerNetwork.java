package com.doohh.akkaClustering.nn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.deeplearning4j.datasets.iterator.AsyncDataSetIterator;
import org.deeplearning4j.datasets.iterator.MultipleEpochsIterator;
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.api.IterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.heartbeat.Heartbeat;
import org.nd4j.linalg.heartbeat.reports.Environment;
import org.nd4j.linalg.heartbeat.reports.Event;
import org.nd4j.linalg.heartbeat.reports.Task;
import org.nd4j.linalg.heartbeat.utils.EnvironmentUtils;
import org.nd4j.linalg.heartbeat.utils.TaskUtils;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.doohh.akkaClustering.dto.AppConf;
import com.doohh.akkaClustering.dto.Command;
import com.doohh.akkaClustering.dto.DistInfo;
import com.doohh.akkaClustering.dto.RouterInfo;
import com.doohh.akkaClustering.util.Nd4jSerialization;
import com.doohh.akkaClustering.worker.Controller;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.pattern.Patterns;
import akka.util.Timeout;
import lombok.Data;
import scala.concurrent.Await;
import scala.concurrent.Future;

@Data
public class SyncDistMultiLayerNetwork extends MultiLayerNetwork {
	private static final Logger log = LoggerFactory.getLogger(DistMultiLayerNetwork.class);

	private String role;
	private DistInfo distInfo;
	private Collection<IterationListener> listeners = new ArrayList<>();
	private Timeout timeout = new Timeout(scala.concurrent.duration.Duration.create(10, "minutes"));
	private Nd4jSerialization nd4jSerialization;

	public SyncDistMultiLayerNetwork(MultiLayerConfiguration conf, DistInfo distInfo2) {
		super(conf);
		this.distInfo = distInfo2;
		this.role = distInfo.getRole();
		nd4jSerialization = new Nd4jSerialization();
	}

	public void init(INDArray parameters, boolean cloneParametersArray) {
		if (layerWiseConfigurations == null || layers == null)
			intializeConfigurations();
		if (initCalled)
			return;

		int nLayers = getnLayers();

		if (nLayers < 1)
			throw new IllegalStateException("Unable to create network: number of layers is less than 1");

		if (this.layers == null || this.layers[0] == null) {
			if (this.layers == null)
				this.layers = new Layer[nLayers];

			// First: Work out total length of (backprop) params
			int backpropParamLength = 0;
			int[] nParamsPerLayer = new int[nLayers];
			for (int i = 0; i < nLayers; i++) {
				NeuralNetConfiguration conf = layerWiseConfigurations.getConf(i);
				nParamsPerLayer[i] = conf.getLayer().initializer().numParams(conf, true);
				backpropParamLength += nParamsPerLayer[i];
			}

			// Create parameters array, if required
			boolean initializeParams;
			if (parameters != null) {
				if (!parameters.isRowVector())
					throw new IllegalArgumentException("Invalid parameters: should be a row vector");
				if (parameters.length() != backpropParamLength)
					throw new IllegalArgumentException("Invalid parameters: expected length " + backpropParamLength
							+ ", got length " + parameters.length());

				if (cloneParametersArray)
					flattenedParams = parameters.dup();
				else
					flattenedParams = parameters;

				initializeParams = false;
			} else {
				flattenedParams = Nd4j.create(1, backpropParamLength);
				initializeParams = true;
			}

			// construct multi-layer
			int paramCountSoFar = 0;
			for (int i = 0; i < nLayers; i++) {
				INDArray paramsView;
				if (nParamsPerLayer[i] > 0) {
					paramsView = flattenedParams.get(NDArrayIndex.point(0),
							NDArrayIndex.interval(paramCountSoFar, paramCountSoFar + nParamsPerLayer[i]));
				} else {
					paramsView = null;
				}
				paramCountSoFar += nParamsPerLayer[i];

				NeuralNetConfiguration conf = layerWiseConfigurations.getConf(i);
				layers[i] = conf.getLayer().instantiate(conf, listeners, i, paramsView, initializeParams);
				layerMap.put(conf.getLayer().getLayerName(), layers[i]);
			}
			initCalled = true;
			initMask();
		}

		// Set parameters in MultiLayerNetwork.defaultConfiguration for later
		// use in BaseOptimizer.setupSearchState() etc
		// Keyed as per backprop()
		defaultConfiguration.clearVariables();
		List<String> variables = defaultConfiguration.variables(false);
		for (int i = 0; i < layers.length; i++) {
			for (String s : layers[i].conf().variables()) {
				variables.add(i + "_" + s);
			}
		}

		// dist init
		distInfo.setParamRange(this.numParams());
		initParamServers(flattenedParams);
		Controller.barrier(distInfo, "slave");
	}

	private void initMask() {
		setMask(Nd4j.ones(1, pack().length()));
	}

	@Override
	public void fit(DataSetIterator iterator) {
		DataSetIterator iter;
		// we're wrapping all iterators into AsyncDataSetIterator to provide
		// background prefetch
		if (!(iterator instanceof AsyncDataSetIterator || iterator instanceof ListDataSetIterator
				|| iterator instanceof MultipleEpochsIterator)) {
			iter = new AsyncDataSetIterator(iterator, 2);
		} else
			iter = iterator;

		// cnn -> false
		if (layerWiseConfigurations.isPretrain()) {
			pretrain(iter);
			iter.reset();
			while (iter.hasNext()) {
				DataSet next = iter.next();
				if (next.getFeatureMatrix() == null || next.getLabels() == null)
					break;
				setInput(next.getFeatureMatrix());
				setLabels(next.getLabels());
				finetune();
			}
		}

		if (layerWiseConfigurations.isBackprop()) {
			if (layerWiseConfigurations.isPretrain())
				iter.reset();
			update(TaskUtils.buildTask(iter));
			iter.reset();

			
			int iteration =0;
			boolean hasNext = false;
			boolean escape = false;			
			while (hasNext = iter.hasNext()) {
				pullParam();
				Controller.barrier(distInfo, "slave");

				DataSet next = iter.next();
				if (next.getFeatureMatrix() == null || next.getLabels() == null)
					break;

				boolean hasMaskArrays = next.hasMaskArrays();

				if (layerWiseConfigurations.getBackpropType() == BackpropType.TruncatedBPTT) {
					doTruncatedBPTT(next.getFeatureMatrix(), next.getLabels(), next.getFeaturesMaskArray(),
							next.getLabelsMaskArray());
				} else {
					if (hasMaskArrays)
						setLayerMaskArrays(next.getFeaturesMaskArray(), next.getLabelsMaskArray());
					setInput(next.getFeatureMatrix());
					setLabels(next.getLabels());
					if (solver == null) {
						// if SGD -> stepFunction =
						// NegativeGradientStepFunction
						// (default)
						solver = new DistSolver.Builder().configure(conf()).listeners(getListeners()).model(this)
								.build();
					}
					solver.optimize();
				}

				if (hasMaskArrays)
					clearLayerMaskArrays();

				// push & pull parameters from master
				pushGrad();

				//Synch
				//System.out.println(distInfo.getRoleIdx() + "'iteration: " + iteration++);
				if (escape = Controller.barrier(distInfo, "slave", hasNext)){
					System.out.println(distInfo.getRoleIdx() + "'escape!!");
					break;					
				}
			}

			// 동기를 맞추기 위해, Iteration을 다른 slave보다 덜 처리한 녀석은 그 차이만큼 Barrier()를
			// 실행해준다.
			// Controller.barrier(distInfo, "slave", cnt);
		}

		// pullParam(); main 문에서 pull한다.
	}

	private void update(Task task) {
		if (!initDone) {
			initDone = true;
			Heartbeat heartbeat = Heartbeat.getInstance();
			task = ModelSerializer.taskByModel(this);
			Environment env = EnvironmentUtils.buildEnvironment();
			heartbeat.reportEvent(Event.STANDALONE, env, task);
		}
	}

	private void initParamServers(INDArray flattenedParams) {
		INDArray param;
		int start, end;
		if (distInfo.getRoleIdx() == 0) {
			int nParamServer = distInfo.getRouterInfo().getNParamServer();
			RouterInfo routerInfo = distInfo.getRouterInfo();
			log.error("{}", routerInfo.getParamComms());
			for (int idx = 0; idx < nParamServer; idx++) {
				ActorSelection as = routerInfo.getParamComms().get(idx);
				try {
					RouterInfo.Range range = routerInfo.getParamRange().get(idx);
					start = range.getStart();
					end = range.getEnd();
					log.error("numParams: {}, start: {}, end: {}", this.numParams(), start, end);
					param = flattenedParams.get(NDArrayIndex.interval(0, 1), NDArrayIndex.interval(start, end));
					Future<Object> future = Patterns.ask(as,
							new Command().setCommand("initParam()").setData(nd4jSerialization.serialize(param)),
							timeout);
					Await.result(future, timeout.duration());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void pushGradPullParam() {
		INDArray gradient;
		int start, end;
		int nParamServer = distInfo.getRouterInfo().getNParamServer();
		RouterInfo routerInfo = distInfo.getRouterInfo();
		for (int idx = 0; idx < nParamServer; idx++) {
			ActorSelection as = routerInfo.getParamComms().get(idx);
			try {
				RouterInfo.Range range = routerInfo.getParamRange().get(idx);
				start = range.getStart();
				end = range.getEnd();
				gradient = this.flattenedGradients.get(NDArrayIndex.interval(0, 1), NDArrayIndex.interval(start, end));
				Future<Object> future = Patterns.ask(as,
						new Command().setCommand("pushGradPullParam()").setData(nd4jSerialization.serialize(gradient)),
						timeout);
				INDArray param = (INDArray) Await.result(future, timeout.duration());
				flattenedParams.get(NDArrayIndex.interval(0, 1), NDArrayIndex.interval(start, end)).assign(param);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void pullParam() {
		int start, end;
		int nParamServer = distInfo.getRouterInfo().getNParamServer();
		RouterInfo routerInfo = distInfo.getRouterInfo();
		for (int idx = 0; idx < nParamServer; idx++) {
			ActorSelection as = routerInfo.getParamComms().get(idx);
			RouterInfo.Range range = routerInfo.getParamRange().get(idx);
			start = range.getStart();
			end = range.getEnd();
			try {
				Future<Object> future = Patterns.ask(as, new Command().setCommand("pullParam()").setData(null),
						timeout);
				INDArray param = (INDArray) nd4jSerialization
						.deserialize((byte[]) Await.result(future, timeout.duration()));
				flattenedParams.get(NDArrayIndex.interval(0, 1), NDArrayIndex.interval(start, end)).assign(param);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private void pushGrad() {
		INDArray gradient;
		int start, end;
		int nParamServer = distInfo.getRouterInfo().getNParamServer();
		RouterInfo routerInfo = distInfo.getRouterInfo();
		for (int idx = 0; idx < nParamServer; idx++) {
			ActorSelection as = routerInfo.getParamComms().get(idx);
			try {
				RouterInfo.Range range = routerInfo.getParamRange().get(idx);
				start = range.getStart();
				end = range.getEnd();
				gradient = this.flattenedGradients.get(NDArrayIndex.interval(0, 1), NDArrayIndex.interval(start, end));
				Future<Object> future = Patterns.ask(as,
						new Command().setCommand("pushGradient()").setData(nd4jSerialization.serialize(gradient)),
						timeout);
				Await.result(future, timeout.duration());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void finishApp(AppConf appConf) {
		ActorSelection controller = distInfo.getController();
		controller.tell(new Command().setCommand("finishApp()").setData(appConf), ActorRef.noSender());
	}

}
