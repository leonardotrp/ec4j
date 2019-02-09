package br.ufrj.coc.cec2015.algorithm.abc;

import java.util.Collections;
import java.util.List;

import br.ufrj.coc.cec2015.algorithm.Individual;
import br.ufrj.coc.cec2015.algorithm.Population;
import br.ufrj.coc.cec2015.algorithm.de.DEHelper;
import br.ufrj.coc.cec2015.util.Helper;
import br.ufrj.coc.cec2015.util.Properties;
import br.ufrj.coc.cec2015.util.Statistic;

public class ABCHelper {

	public enum ABCVariant {
		ABC, MABC, GbABC, GbdABC, AloABC
	}
	private static ABCVariant VARIANT;
	static {
		VARIANT = ABCVariant.valueOf(ABCProperties.VARIANT);
	}
	
	public static double calculateFitness(double functionValue) {
		double fitness = 0;
		if (functionValue >= 0) {
			fitness = 1 / (functionValue + 1);
		} else {
			fitness = 1 + Math.abs(functionValue);
		}
		return fitness;
	}
	
	private static double random() {
		//return ((double) Math.random() * 32767 / ((double) (32767) + (double) (1)));
		return Helper.randomInRange(0.0, 1.0);
	}
	
	private static int getIndexForChange() {
		/* The parameter to be changed is determined randomly */
		return (int) (random() * Properties.INDIVIDUAL_SIZE);
	}
	
	private static double getEuclidianDistance(Individual food1, Individual food2) {
		double distance = 0.0;
		for (int index = 0; index < Properties.INDIVIDUAL_SIZE; index++) {
			double p = food1.get(index);
			double q = food2.get(index);
			distance += Math.pow((p - q), 2);
		}
		return Math.sqrt(distance);
	}
	
	private static double getProbabilityByEuclidianDistance(Population foodSources, int i, int k) {
		Individual foodI = foodSources.get(i);
		Individual foodK = foodSources.get(k);
		
		double numerator = (double) (1 / getEuclidianDistance(foodI, foodK));
		
		double denominator = 0.0;
		for (int indexL = 0; indexL < Properties.POPULATION_SIZE; indexL++) {
			if (indexL != i) {
				Individual foodL = foodSources.get(indexL);
				denominator += (double) (1 / getEuclidianDistance(foodI, foodL));
			}
		}
		
		return (numerator / denominator);
	}
	
	private static void calculateProbabilitiesByEuclidianDistance(Population foodSources, int i) {
		double probability;
		for (int k = 0; k < Properties.POPULATION_SIZE; k++) {
			if (k != i) {
				probability = getProbabilityByEuclidianDistance(foodSources, i, k);
				foodSources.get(k).setProbability(probability);
			}
		}
	}
	
	private static int getNeighbourIndex(Population foodSources, Individual food) {
		int currentIndex = foodSources.indexOf(food); // i
		
		// A randomly chosen solution is used in producing a mutant solution of the solution i
		int neighbourIndex = (int) (random() * Properties.POPULATION_SIZE); // K index
		while (neighbourIndex == currentIndex) {
			neighbourIndex = (int) (random() * Properties.POPULATION_SIZE); // K index
		}
		return neighbourIndex;
	}
	
	private static int getNeighbourIndexByProbability(Population foodSources, Individual food) throws Exception {
		Population sortedFoodSources = (Population) foodSources.cloneAll();
		int i = foodSources.indexOf(food); // i
		calculateProbabilitiesByEuclidianDistance(sortedFoodSources, i);
	
		List<Individual> foods = sortedFoodSources.getIndividuals();
		Collections.sort(foods, new ComparatorByProbability());

		// roda a roleta a partir de um vetor cumulativo de probabilidades
		double[] cumulativeVector = new double[Properties.POPULATION_SIZE];
		cumulativeVector[0] = foods.get(0).getProbability();
		for (int indexProb = 1; indexProb < Properties.POPULATION_SIZE; indexProb++) {
			cumulativeVector[indexProb] = cumulativeVector[indexProb - 1] + foods.get(indexProb).getProbability();  
		}
		
		double pin = Helper.randomInRange(0.0, 1.0);
		int indexResult = 0;
		for (int index = 0; index < Properties.POPULATION_SIZE; index++) {
			if (cumulativeVector[index] >= pin) {
				indexResult = index;
				break;
			}
		}
		return indexResult;
	}
	
	private static Individual getNeighbour(Population foodSources, Individual food) throws Exception {
		int indexNeighbour = 0;
		if (VARIANT.equals(ABCVariant.GbdABC)) {
			indexNeighbour = getNeighbourIndexByProbability(foodSources, food);
		}
		else
			indexNeighbour = getNeighbourIndex(foodSources, food);

		return foodSources.get(indexNeighbour); // X(i,k)
	}

	private static boolean canMutate() {
		return !VARIANT.equals(ABCVariant.MABC) || random() < ABCProperties.MR;
	}
	
	private static boolean isGbestVariation() {
		return (VARIANT.equals(ABCVariant.GbABC) || VARIANT.equals(ABCVariant.GbdABC) || VARIANT.equals(ABCVariant.AloABC));
	}
	
	private static double mutate(int indexForChange/*j*/, Population foodSources, Individual food /*X(i)*/, Individual neighbour/*X(k)*/) {
		double mutatedValue = food.get(indexForChange); // X(i,j)

		if (canMutate()) {
			double phi = Helper.randomInRange(-1.0, 1.0); //(random() - 0.5) * 2;
			mutatedValue += phi * (food.get(indexForChange) /*X(i,j)*/ - neighbour.get(indexForChange) /*X(k,j)*/);
		}

		if (isGbestVariation()) {
			double sigma = Helper.randomInRange(0.0, ABCProperties.C);
			mutatedValue += sigma * (foodSources.getBest().get(indexForChange) /*X(gbest,j)*/ - food.get(indexForChange) /*X(i,j)*/);
		}

		if (mutatedValue < Properties.SEARCH_RANGE[0])
			mutatedValue = Properties.SEARCH_RANGE[0];
		if (mutatedValue > Properties.SEARCH_RANGE[1])
			mutatedValue = Properties.SEARCH_RANGE[1];
		
		return mutatedValue;
	}
	
	private static double[] generateAloTrialVector(Population foodSources, Individual food, Individual neighbour) {
		double[] trialVectorId = new double[Properties.INDIVIDUAL_SIZE];
		for (int j = 0; j < Properties.INDIVIDUAL_SIZE; j++) {
			int randI = Helper.randomInRange(0, Properties.INDIVIDUAL_SIZE - 1);
			if (DEHelper.canCrossover(food) || j == randI) {
				trialVectorId[j] = mutate(j, foodSources, food, neighbour);
			}
			else {
				trialVectorId[j] = food.get(j);
			}
		}
		return trialVectorId;
	}
	
	private static double[] generateTrialVector(Population foodSources, Individual food /*X(i)*/) throws Exception {
		Individual neighbour = getNeighbour(foodSources, food); // X(i,k)

		if (VARIANT.equals(ABCVariant.AloABC)) {
			// At least one (ALO) modification
			return generateAloTrialVector(foodSources, food, neighbour);
		}
		else {
			// Only one modification
			double[] trialVectorId = food.getId().clone();
			int indexForChange = getIndexForChange(); // j
			trialVectorId[indexForChange] = mutate(indexForChange, foodSources, food, neighbour);
			return trialVectorId;
		}
	}

	private static boolean foodChanged(Individual food, double[] trialVector) {
		double functionValue = Helper.evaluate(trialVector);
		double fitnessValue = ABCHelper.calculateFitness(functionValue);

		// a greedy selection is applied between the current solution i and its mutant
		if (fitnessValue > food.getFitness()) {
			// If the mutant solution is better than the current solution i, replace the solution with the mutant and reset the trial counter of solution i
			food.resetTrial();
			food.setId(trialVector);
			food.setFunctionValue(functionValue);
			food.setFitness(fitnessValue);
			
			return true;
			
		} else {
			// if the solution i can not be improved, increase its trial counter
			food.incrementTrial();
			return false;
		}
	}
	
	public static void sendEmployedBees(Population foodSources, Statistic statistic, int round) throws Exception {

		// send employee bees
		for (int index = 0; index < foodSources.size(); index++) {
			
			Individual food = foodSources.get(index);
			
			double[] trialVector = generateTrialVector(foodSources, food);/*DEHelper.generateTrialVectorBinomial(foodSources, foodSources.indexOf(food));*/
			
			if (foodChanged(food, trialVector))
				foodSources.updateBestError(food);

			statistic.verifyEvaluationInstant(round, foodSources);
		}
	}
	
	private static void calculateProbabilities(Population foodSources) {
		int indexFood;
		double maxFitness = -1;
		Individual food;
		// calculate the maximum fitness value
		for (indexFood = 0; indexFood < foodSources.size(); indexFood++) {
			food = foodSources.get(0);
			if (food.getFitness() > maxFitness)
				maxFitness = food.getFitness();
		}

		for (indexFood = 0; indexFood < foodSources.size(); indexFood++) {
			food = foodSources.get(0);
			double probability = (0.9 * (food.getFitness() / maxFitness)) + 0.1;
			food.setProbability(probability);
		}
	}

	public static void sendOnLookerBees(Population foodSources, Statistic statistic, int round) throws Exception {
		calculateProbabilities(foodSources);

		int indexFood = 0, currentFood = 0;

		/* onlooker Bee Phase */
		while (currentFood < foodSources.size()) {

			Individual food = foodSources.get(indexFood);
			
			if (random() < food.getProbability()) { /* choose a food source depending on its probability to be chosen */

				currentFood++;

				double[] trialVector = generateTrialVector(foodSources, food);
				
				if (foodChanged(food, trialVector))
					foodSources.updateBestError(food);

				statistic.verifyEvaluationInstant(round, foodSources);
				
			} /* if */
			indexFood++;
			if (indexFood == foodSources.size())
				indexFood = 0;
		} /* while */

		/* end of onlooker bee phase */
	}
	
	public static Individual newInstanceFood() {
		Individual foodSource = new Individual(Properties.INDIVIDUAL_SIZE);
		double functionValue = Helper.evaluate(foodSource.getId());
		double fitnessValue = ABCHelper.calculateFitness(functionValue);
		foodSource.setFunctionValue(functionValue);
		foodSource.setFitness(fitnessValue);

		return foodSource;
	}
	
	public static void sendScoutBees(Population foodSources, Statistic statistic, int round) {
		int maxTrialIndex = 0;
		Individual foodMaxTrial;
		for (int indexFood = 1; indexFood < foodSources.size(); indexFood++) {
			foodMaxTrial = foodSources.get(maxTrialIndex);
			Individual food = foodSources.get(indexFood);
			if (food.getTrial() > foodMaxTrial.getTrial())
				maxTrialIndex = indexFood;
		}

		foodMaxTrial = foodSources.get(maxTrialIndex);
		if (foodMaxTrial.getTrial() >= ABCProperties.MAX_TRIAL) {
			foodSources.initializeIndividual(maxTrialIndex);

			statistic.verifyEvaluationInstant(round, foodSources);
		}
	}	
}
