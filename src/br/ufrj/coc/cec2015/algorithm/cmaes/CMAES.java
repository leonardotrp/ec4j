package br.ufrj.coc.cec2015.algorithm.cmaes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import br.ufrj.coc.cec2015.algorithm.Algorithm;
import br.ufrj.coc.cec2015.algorithm.AlgorithmHelper;
import br.ufrj.coc.cec2015.algorithm.Individual;
import br.ufrj.coc.cec2015.algorithm.Population;
import br.ufrj.coc.cec2015.algorithm.cmaes.fr.inria.CMAEvolutionStrategy;
import br.ufrj.coc.cec2015.algorithm.cmaes.fr.inria.CMASolution;
import br.ufrj.coc.cec2015.math.MatrixUtil;
import br.ufrj.coc.cec2015.util.Helper;
import br.ufrj.coc.cec2015.util.Properties;
import br.ufrj.coc.cec2015.util.Statistic;

public class CMAES extends Algorithm {

	@Override
	public String[] getVariants() {
		return new String[] {"IPOP"};
	}

	@Override
	protected AlgorithmHelper newInstanceHelper() {
		return null;
	}
	
	@Override
	public void run(Population population, Statistic statistic, int round) throws Exception {
		int irun, nbRuns=1;  // restarts, re-read from properties file below
        double [] fitness; 
        CMASolution bestSolution = null; // initialization to allow compilation
        long counteval = Properties.ARGUMENTS.get().getCountEvaluations();   // variables used for restart
        int lambda = 0;

		for (irun = 0; irun < nbRuns; irun++) {

			CMAEvolutionStrategy cma = new CMAEvolutionStrategy();

            if (population.getEigenvectors() == null) {
            	population.setEigenvectors(MatrixUtil.getEigenDecomposition(population).getV());
            	cma.setPopulation(population.toMatrix());
            }
			
	    	// read properties file and obtain some values for "private" use
	    	cma.readProperties(); // reads from file CMAEvolutionStrategy.properties
	    	cma.setDimension(Properties.ARGUMENTS.get().getIndividualSize());
	    	cma.setInitialX(Properties.SEARCH_RANGE[0], Properties.SEARCH_RANGE[1]);
	    	cma.options.stopMaxFunEvals = Properties.ARGUMENTS.get().getMaxFES();
	    	cma.options.stopFitness = Properties.MIN_ERROR_VALUE;
	    	//cma.parameters.setPopulationSize(Properties.POPULATION_SIZE);// ==> λ = 4 + [3.log N] (CMAParameters.java - Linha 195)
	    	cma.setInitialStandardDeviation(0.3);
			
			// set up fitness function
	    	//double nbFunc = cma.options.getFirstToken(cma.getProperties().getProperty("functionNumber"), 10);
	    	//int rotate = cma.options.getFirstToken(cma.getProperties().getProperty("functionRotate"), 0);
	    	//double axisratio = cma.options.getFirstToken(cma.getProperties().getProperty("functionAxisRatio"), 0.);
	        //IObjectiveFunction fitfun = new FunctionCollector(nbFunc, rotate, axisratio);

			// set up restarts
	        nbRuns = 1+cma.options.getFirstToken(cma.getProperties().getProperty("numberOfRestarts"), 1);
	        double incPopSizeFactor = cma.options.getFirstToken(cma.getProperties().getProperty("incPopSizeFactor"), 1.);
	         
	        // initialize
	        if (irun == 0) {
	        	fitness = cma.init(); // finalize setting of population size lambda, get fitness array
	        	lambda = cma.parameters.getPopulationSize(); // retain lambda for restart
	    		cma.writeToDefaultFilesHeaders(0); // overwrite output files
	    	}
	    	else {
	    		int newPopulationSize = (int)Math.ceil(lambda * Math.pow(incPopSizeFactor, irun));
	            cma.parameters.setPopulationSize(newPopulationSize);
	            cma.setCountEval(counteval); // somehow a hack 
	            fitness = cma.init(); // provides array to assign fitness values
	            System.err.println("Increase population size from " + cma.parameters.getPopulationSize() + " to " + newPopulationSize);
	        }

	        // set additional termination criterion
	        if (nbRuns > 1) 
	           cma.options.stopMaxIter = (long) (100 + 200*Math.pow(cma.getDimension(),2)*Math.sqrt(cma.parameters.getLambda()));
			
			double lastTime = 0, alastTime = 0; // for smarter console output
            while(cma.stopConditions.isFalse()) {
		        // --- core iteration step ---

            	double[][] pop = cma.samplePopulation(); // get a new population of solutions
		        population.load(pop);
		        for(int i = 0; i < population.size(); ++i) {    // for each candidate solution i
		        	// a simple way to handle constraints that define a convex feasible domain  
		        	// (like box constraints, i.e. variable boundaries) via "blind re-sampling" 
		        	                                       // assumes that the feasible domain is convex, the optimum is  
					//while (!fitfun.isFeasible(pop[i]))     //   not located on (or very close to) the domain boundary,
		        	double[] resample = cma.resampleSingle(i); //   initialX is feasible and initialStandardDeviations are sufficiently small to prevent quasi-infinite looping here
		            // compute fitness/objective value
		        	double functionValue = Properties.ARGUMENTS.get().evaluateFunction(resample);
		        	
		        	Individual current = population.get(i);
					current.setId(resample);
					current.setFunctionValue(functionValue);
					population.updateBestError(current);
		
					statistic.verifyEvaluationInstant(round, population);
		        	
		        	fitness[i] = Helper.getError(functionValue); // fitfun.valueOf() is to be minimized
		        }
		        cma.updateDistribution(fitness);         // pass fitness array to update search distribution
		        // --- end core iteration step ---

		        // stopping conditions can be changed in file CMAEvolutionStrategy.properties 
		        cma.readProperties();
		
		        // the remainder is output
		        cma.writeToDefaultFiles();
		
		        // screen output
		        boolean printsomething = true; // for a convenient switch to false
		        if (printsomething && System.currentTimeMillis() - alastTime > 20e3) {
		            cma.printlnAnnotation();
		            alastTime = System.currentTimeMillis();
		        }
		        if (printsomething && (cma.stopConditions.isTrue() || cma.getCountIter() < 4 
		                || (cma.getCountIter() > 0 && (Math.log10(cma.getCountIter()) % 1) < 1e-11)
		                || System.currentTimeMillis() - lastTime > 2.5e3)) { // wait 2.5 seconds
		            cma.println();
		            lastTime = System.currentTimeMillis();
		        }
            } // iteration loop
            
    		// evaluate mean value as it is the best estimator for the optimum
            //double funcValMeanX = Helper.evaluate(cma.getMeanX());
    		//cma.setFitnessOfMeanX(Helper.getError(funcValMeanX)); // updates the best ever solution 

    		// retain best solution ever found 
    		if (irun == 0)
    			bestSolution = cma.getBestSolution();
    		else if (cma.getBestSolution().getFitness() < bestSolution.getFitness())
    			bestSolution = cma.getBestSolution();

            // final output for the run
            cma.writeToDefaultFiles(1); // 1 == make sure to write final result
            cma.println("Terminated (run " + (irun+1) + ") due to");
            for (String s : cma.stopConditions.getMessages()) 
                cma.println("      " + s);
    		cma.println("    best function value " + cma.getBestFunctionValue() 
    				+ " at evaluation " + cma.getBestEvaluationNumber());

            // quit restart loop if MaxFunEvals or target Fitness are reached
            boolean quit = false;
            for (String s : cma.stopConditions.getMessages()) 
                if (s.startsWith("MaxFunEvals") ||
                    s.startsWith("Fitness")) 
                    quit = true;
            if (quit)
                break;
            
            counteval = cma.getCountEval();

            if (irun < nbRuns-1) // after Manual stop give time out to change stopping condition 
            	for (String s : cma.stopConditions.getMessages()) 
            		if (s.startsWith("Manual")) {
            			System.out.println("incomment 'stop now' and press return to start next run");
            			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            			try { in.readLine(); }
            			catch(IOException e) { System.out.println("input not readable"); }
            		}

        } // for irun < nbRuns

        // screen output
        if (irun > 1) {
            System.out.println(" " + (irun) + " runs conducted," 
                    + " best function value " + bestSolution.getFitness() 
                    + " at evaluation " + bestSolution.getEvaluationNumber());
        }
	}
}
