package com.doohh.akkaClustering.nn;

import java.util.Collection;

import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.optimize.Solver;
import org.deeplearning4j.optimize.api.ConvexOptimizer;
import org.deeplearning4j.optimize.api.IterationListener;
import org.deeplearning4j.optimize.api.StepFunction;
import org.deeplearning4j.optimize.solvers.ConjugateGradient;
import org.deeplearning4j.optimize.solvers.LBFGS;
import org.deeplearning4j.optimize.solvers.LineGradientDescent;
import org.deeplearning4j.optimize.solvers.StochasticGradientDescent;

public class DistSolver extends Solver {
    private NeuralNetConfiguration conf;
    private Collection<IterationListener> listeners;
    private Model model;
    private ConvexOptimizer optimizer;
    private StepFunction stepFunction;
	
    public void optimize() {
        if(optimizer == null)
            optimizer = getOptimizer();
        optimizer.optimize();

    }

    public ConvexOptimizer getOptimizer() {
        if(optimizer != null) return optimizer;
        switch(conf.getOptimizationAlgo()) {
            case LBFGS:
                optimizer = new LBFGS(conf,stepFunction,listeners,model);
                break;
            case LINE_GRADIENT_DESCENT:
                optimizer = new LineGradientDescent(conf,stepFunction,listeners,model);
                break;
            case CONJUGATE_GRADIENT:
                optimizer = new ConjugateGradient(conf,stepFunction,listeners,model);
                break;
            case STOCHASTIC_GRADIENT_DESCENT:
                optimizer = new StochasticGradientDescent(conf,stepFunction,listeners,model);
                break;
            default:
                throw new IllegalStateException("No optimizer found");
        }
        return optimizer;
    }
}
