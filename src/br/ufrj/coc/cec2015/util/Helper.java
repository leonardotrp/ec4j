package br.ufrj.coc.cec2015.util;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import br.ufrj.coc.cec2015.algorithm.Individual;
import br.ufrj.coc.cec2015.algorithm.Population;

public class Helper {
	
	public static boolean terminateRun(Population population) {
		if (population.size() > 0) {
			double errorValue = population.getBestError();
			population.setMinErrorValueFound(errorValue <= Properties.MIN_ERROR_VALUE);
			return population.isMinErrorValueFound() || Properties.ARGUMENTS.get().isMaxFESReached();
		}
		return false;
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
	
	public static double getError(Individual individual) {
		return getError(individual.getFunctionValue());
	}
	
	public static double getError(double functionValue) {
		double optimumValue = Properties.ARGUMENTS.get().getFunctionNumber() * 100;
		return Math.abs(optimumValue - functionValue);
	}
		
	public static Individual newIndividualInitialized() {
		Individual individual = new Individual(Properties.ARGUMENTS.get().getIndividualSize());
		double functionValue = Properties.ARGUMENTS.get().evaluateFunction(individual.getId());
		individual.setFunctionValue(functionValue);
		return individual;
	}
}
