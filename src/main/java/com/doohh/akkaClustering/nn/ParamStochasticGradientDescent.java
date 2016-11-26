package com.doohh.akkaClustering.nn;

import java.util.Collection;

import org.deeplearning4j.berkeley.Pair;
import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.gradient.Gradient;
import org.deeplearning4j.optimize.api.IterationListener;
import org.deeplearning4j.optimize.api.StepFunction;
import org.deeplearning4j.optimize.api.TerminationCondition;
import org.deeplearning4j.optimize.solvers.StochasticGradientDescent;
import org.nd4j.linalg.api.ndarray.INDArray;

public class ParamStochasticGradientDescent extends StochasticGradientDescent {
	public ParamStochasticGradientDescent(NeuralNetConfiguration conf, StepFunction stepFunction, Collection<IterationListener> iterationListeners, Model model) {
        super(conf, stepFunction, iterationListeners, model);
    }

    public ParamStochasticGradientDescent(NeuralNetConfiguration conf, StepFunction stepFunction, Collection<IterationListener> iterationListeners, Collection<TerminationCondition> terminationConditions, Model model) {
        super(conf, stepFunction, iterationListeners, terminationConditions, model);
    }

    @Override
    public boolean optimize() {
//    	 INDArray params = model.params();
//         stepFunction.step(params,gradient.gradient());
//         model.setParams(params);
         
/*        for(int i = 0; i < conf.getNumIterations(); i++) {

            Pair<Gradient,Double> pair = gradientAndScore();
            Gradient gradient = pair.getFirst();

            INDArray params = model.params();
            stepFunction.step(params,gradient.gradient());
            //Note: model.params() is always in-place for MultiLayerNetwork and ComputationGraph, hence no setParams is necessary there
            //However: for pretrain layers, params are NOT a view. Thus a setParams call is necessary
            //But setParams should be a no-op for MLN and CG
            model.setParams(params);

            for(IterationListener listener : iterationListeners)
                listener.iterationDone(model, i);

            checkTerminalConditions(pair.getFirst().gradient(), oldScore, score, i);

            iteration++;
        }*/
        return true;
    }
}
