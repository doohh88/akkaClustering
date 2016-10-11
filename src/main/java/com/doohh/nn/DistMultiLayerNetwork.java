package com.doohh.nn;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import org.deeplearning4j.datasets.iterator.AsyncDataSetIterator;
import org.deeplearning4j.datasets.iterator.MultipleEpochsIterator;
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.layers.factory.LayerFactories;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.Solver;
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

import com.doohh.akkaClustering.util.AppNetInfo;
import com.doohh.akkaClustering.util.PropFactory;
import com.doohh.akkaClustering.util.Util;
import com.doohh.akkaClustering.worker.WorkerMain;

import akka.actor.ActorSelection;

public class DistMultiLayerNetwork extends MultiLayerNetwork {

	private Collection<IterationListener> listeners = new ArrayList<>();
	private static Properties props;
	private String role = null;
	private String roleIdx = null;
	private AppNetInfo appNetInfo = null;
	private ActorSelection task = null;

	public DistMultiLayerNetwork(MultiLayerConfiguration conf) {
		super(conf);
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
				nParamsPerLayer[i] = LayerFactories.getFactory(conf).initializer().numParams(conf, true);
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
				layers[i] = LayerFactories.getFactory(conf).create(conf, listeners, i, paramsView, initializeParams);
				layerMap.put(conf.getLayer().getLayerName(), layers[i]);
			}
			initCalled = true;
			initMask();
		}

		// Set parameters in MultiLayerNetwork.defaultConfiguration for later
		// use in BaseOptimizer.setupSearchState() etc
		// Keyed as per backprop()
		defaultConfiguration.clearVariables();
		for (int i = 0; i < layers.length; i++) {
			for (String s : layers[i].conf().variables()) {
				defaultConfiguration.addVariable(i + "_" + s);
			}
		}

		// init dist configuration
		// load task.properties & network
		loadTaskProp();
	}

	private void initMask() {
		setMask(Nd4j.ones(1, pack().length()));
	}

	private void loadTaskProp() {
		String path = Util.getHomeDir() + "/conf";
		File[] fileList = Util.getFileList(path);
		for (File file : fileList) {
			if (file.getName().contains("task")) {
				props = PropFactory.getInstance(file.getName()).getProperties();
				break;
			}
		}

		this.role = props.getProperty("role");
		this.roleIdx = props.getProperty("roleIdx");
		this.task = WorkerMain.actorSystem.actorSelection(props.getProperty("task"));
		setAppNetInfo();
	}

	private void setAppNetInfo() {
		this.appNetInfo = new AppNetInfo();
		String paramAddrs = props.getProperty("paramNodes");
		this.appNetInfo.getParamAddr().toArray(new String(paramAddrs).split(","));
		String slaveAddrs = props.getProperty("slaveNodes");
		this.appNetInfo.getParamAddr().toArray(new String(slaveAddrs).split(","));
		this.appNetInfo.setActorSelection();
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
			while (iter.hasNext()) {
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
						solver = new Solver.Builder().configure(conf()).listeners(getListeners()).model(this).build();
					}
					solver.optimize();
				}

				if (hasMaskArrays)
					clearLayerMaskArrays();
			}
		}
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

}
