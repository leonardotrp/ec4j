package br.ufrj.coc.cec2015.util;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import br.ufrj.coc.cec2015.algorithm.Individual;
import br.ufrj.coc.cec2015.algorithm.Population;
import br.ufrj.coc.cec2015.functions.testfunc;

public class Helper {
	public static final testfunc FITNESS_FUNCTIONS = new testfunc();
	public static int COUNT_EVALUATIONS = 0;

	public static void initializeRun() {
		COUNT_EVALUATIONS = 0;
	}

	public static void changeFunction(int dim, int functionNumber) {
		Properties.FUNCTION_NUMBER = functionNumber;
		try {
			Helper.FITNESS_FUNCTIONS.loadConstants(dim, functionNumber);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static double evaluate(double[] input) {
		double functionValue = 0.0;
		try {
			functionValue = Helper.FITNESS_FUNCTIONS.exec_func(input, input.length, Properties.FUNCTION_NUMBER);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		COUNT_EVALUATIONS++;
		return functionValue;
	}
	
	public static boolean terminateRun(Population population) {
		double errorValue = population.getBestError();
		boolean terminate = Helper.COUNT_EVALUATIONS >= Properties.MAX_FES || errorValue <= Properties.MIN_ERROR_VALUE;
		if (terminate) {
			population.setMinErrorValueFound(errorValue <= Properties.MIN_ERROR_VALUE);
		}
		return terminate;
	}
	
	private static Random random = new Random();

	public static double randomInRange(double min, double max) {
		double range = max - min;
		double scaled = random.nextDouble() * range;
		return scaled + min;
	}
	
	public static int randomInRange(int min, int max) {
		int randomIndex = ThreadLocalRandom.current().nextInt(min, max + 1);
		return randomIndex;
	}
	
	public static double randomData() {
		double random = randomInRange(Properties.SEARCH_RANGE[0], Properties.SEARCH_RANGE[1]);
		return random;
	}
	
	public static double getBestError(Population population) {
		double best = Double.MAX_VALUE;
		for (int index = 0; index < population.size(); index++) {
			double error = getError(population.get(index));
			if (index == 0 || error < best) {
				best = error;
			}
		}
		return best;
	}
	
	public static double getError(Individual individual) {
		double globalOptimum = Properties.FUNCTION_NUMBER * 100;
		return Math.abs(globalOptimum - individual.getFunctionValue());
	}

	public static Individual newIndividualInitialized() {
		Individual individual = new Individual(Properties.INDIVIDUAL_SIZE);
		double functionValue = Helper.evaluate(individual.getId());
		individual.setFunctionValue(functionValue);
		//individual.setFunctionValueBestKnow(functionValue);

		return individual;
	}
}
