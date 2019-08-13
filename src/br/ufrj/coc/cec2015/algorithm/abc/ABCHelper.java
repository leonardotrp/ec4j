package br.ufrj.coc.cec2015.algorithm.abc;

import java.util.Collections;
import java.util.List;

import br.ufrj.coc.cec2015.algorithm.AlgorithmHelper;
import br.ufrj.coc.cec2015.algorithm.Individual;
import br.ufrj.coc.cec2015.algorithm.Population;
import br.ufrj.coc.cec2015.util.Helper;
import br.ufrj.coc.cec2015.util.Properties;
import br.ufrj.coc.cec2015.util.Statistic;

public class ABCHelper implements AlgorithmHelper {
	private Population foodSources;
	
	@Override
	public void initializeGeneration(Population foodSources) {
		this.foodSources = foodSources;
	}

	private static double calculateFitness(double functionValue) {
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
		return (int) (random() * Properties.ARGUMENTS.get().getIndividualSize());
	}
	
	private static double getEuclidianDistance(Individual food1, Individual food2) {
		double distance = 0.0;
		for (int index = 0; index < Properties.ARGUMENTS.get().getIndividualSize(); index++) {
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
		for (int indexL = 0; indexL < Properties.ARGUMENTS.get().getPopulationSize(); indexL++) {
			if (indexL != i) {
				Individual foodL = foodSources.get(indexL);
				denominator += (double) (1 / getEuclidianDistance(foodI, foodL));
			}
		}
		
		return (numerator / denominator);
	}
	
	private static void calculateProbabilitiesByEuclidianDistance(Population foodSources, int i) {
		double probability;
		for (int k = 0; k < Properties.ARGUMENTS.get().getPopulationSize(); k++) {
			if (k != i) {
				probability = getProbabilityByEuclidianDistance(foodSources, i, k);
				foodSources.get(k).setProbability(probability);
			}
		}
	}
	
	private int getNeighbourIndex(Individual food) {
		int currentIndex = this.foodSources.indexOf(food); // i
		
		// A randomly chosen solution is used in producing a mutant solution of the solution i
		int neighbourIndex = (int) (random() * Properties.ARGUMENTS.get().getPopulationSize()); // K index
		while (neighbourIndex == currentIndex) {
			neighbourIndex = (int) (random() * Properties.ARGUMENTS.get().getPopulationSize()); // K index
		}
		return neighbourIndex;
	}
	
	private int getNeighbourIndexByProbability(Individual food) throws Exception {
		Population sortedFoodSources = (Population) this.foodSources.cloneAll();
		int i = foodSources.indexOf(food); // i
		calculateProbabilitiesByEuclidianDistance(sortedFoodSources, i);
	
		List<Individual> foods = sortedFoodSources.getIndividuals();
		Collections.sort(foods, new ComparatorByProbability());

		// roda a roleta a partir de um vetor cumulativo de probabilidades
		int populationSize = Properties.ARGUMENTS.get().getPopulationSize();
		double[] cumulativeVector = new double[populationSize];
		cumulativeVector[0] = foods.get(0).getProbability();
		for (int indexProb = 1; indexProb < populationSize; indexProb++) {
			cumulativeVector[indexProb] = cumulativeVector[indexProb - 1] + foods.get(indexProb).getProbability();  
		}
		
		double pin = Helper.randomInRange(0.0, 1.0);
		int indexResult = 0;
		for (int index = 0; index < populationSize; index++) {
			if (cumulativeVector[index] >= pin) {
				indexResult = index;
				break;
			}
		}
		return indexResult;
	}
	
	private Individual getNeighbour(Individual food) throws Exception {
		int indexNeighbour = 0;
		if (ABCProperties.isGbdABC()) {
			indexNeighbour = getNeighbourIndexByProbability(food);
		}
		else
			indexNeighbour = getNeighbourIndex(food);

		return this.foodSources.get(indexNeighbour); // X(i,k)
	}

	private static boolean canMutate() {
		return !ABCProperties.isMABC() || random() < ABCProperties.MR;
	}
	
	private static boolean isGbestVariation() {
		return ABCProperties.isGbABC() || ABCProperties.isGbdABC() || ABCProperties.isAloABC();
	}
	
	private double mutate(int indexForChange/*j*/, Individual food /*X(i)*/, Individual neighbour/*X(k)*/) {
		double mutatedValue = food.get(indexForChange); // X(i,j)

		if (canMutate()) {
			double phi = Helper.randomInRange(-1.0, 1.0); //(random() - 0.5) * 2;
			mutatedValue += phi * (food.get(indexForChange) /*X(i,j)*/ - neighbour.get(indexForChange) /*X(k,j)*/);
		}

		if (isGbestVariation()) {
			double sigma = Helper.randomInRange(0.0, ABCProperties.C);
			mutatedValue += sigma * (this.foodSources.getBest().get(indexForChange) /*X(gbest,j)*/ - food.get(indexForChange) /*X(i,j)*/);
		}

		double[] range = Properties.getSearchRange();
		if (mutatedValue < range[0])
			mutatedValue = range[0];
		if (mutatedValue > range[1])
			mutatedValue = range[1];
		
		return mutatedValue;
	}
	
	private double[] generateAloTrialVector(Individual food, Individual neighbour) {
		double[] trialVectorId = new double[Properties.ARGUMENTS.get().getIndividualSize()];
		for (int j = 0; j < Properties.ARGUMENTS.get().getIndividualSize(); j++) {
			int randI = Helper.randomInRange(0, Properties.ARGUMENTS.get().getIndividualSize() - 1);
			if (Math.random() <= food.getCrossoverRate() || j == randI) {
				trialVectorId[j] = mutate(j, food, neighbour);
			}
			else {
				trialVectorId[j] = food.get(j);
			}
		}
		return trialVectorId;
	}
	
	private double[] generateTrialVector(Individual food /*X(i)*/) throws Exception {
		Individual neighbour = getNeighbour(food); // X(i,k)

		if (ABCProperties.isAloABC()) {
			// At least one (ALO) modification
			return generateAloTrialVector(food, neighbour);
		}
		else {
			// Only one modification
			double[] trialVectorId = food.getId().clone();
			int indexForChange = getIndexForChange(); // j
			trialVectorId[indexForChange] = mutate(indexForChange, food, neighbour);
			return trialVectorId;
		}
	}

	private static boolean foodChanged(Individual food, double[] trialVector) {
		double functionValue = Properties.ARGUMENTS.get().evaluateFunction(trialVector);
		double fitnessValue = calculateFitness(functionValue);

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
	
	public void sendEmployedBees(Statistic statistic, int round) throws Exception {

		// send employee bees
		for (int index = 0; index < this.foodSources.size(); index++) {
			
			Individual food = this.foodSources.get(index);
			
			double[] trialVector = generateTrialVector(food);/*DEHelper.generateTrialVectorBinomial(foodSources, foodSources.indexOf(food));*/
			
			if (foodChanged(food, trialVector))
				this.foodSources.updateBestError(food);

			statistic.verifyEvaluationInstant(round, this.foodSources);
		}
	}
	
	private void calculateProbabilities() {
		int indexFood;
		double maxFitness = -1;
		Individual food;
		// calculate the maximum fitness value
		for (indexFood = 0; indexFood < this.foodSources.size(); indexFood++) {
			food = this.foodSources.get(0);
			if (food.getFitness() > maxFitness)
				maxFitness = food.getFitness();
		}

		for (indexFood = 0; indexFood < this.foodSources.size(); indexFood++) {
			food = this.foodSources.get(0);
			double probability = (0.9 * (food.getFitness() / maxFitness)) + 0.1;
			food.setProbability(probability);
		}
	}

	public void sendOnLookerBees(Statistic statistic, int round) throws Exception {
		calculateProbabilities();

		int indexFood = 0, currentFood = 0;

		/* onlooker Bee Phase */
		while (currentFood < this.foodSources.size()) {

			Individual food = this.foodSources.get(indexFood);
			
			if (random() < food.getProbability()) { /* choose a food source depending on its probability to be chosen */

				currentFood++;

				double[] trialVector = generateTrialVector(food);
				
				if (foodChanged(food, trialVector))
					this.foodSources.updateBestError(food);

				statistic.verifyEvaluationInstant(round, this.foodSources);
				
			} /* if */
			indexFood++;
			if (indexFood == this.foodSources.size())
				indexFood = 0;
		} /* while */

		/* end of onlooker bee phase */
	}
	
	public static Individual newInstanceFood() {
		Individual foodSource = new Individual(Properties.ARGUMENTS.get().getIndividualSize());
		double functionValue = Properties.ARGUMENTS.get().evaluateFunction(foodSource.getId());
		double fitnessValue = calculateFitness(functionValue);
		foodSource.setFunctionValue(functionValue);
		foodSource.setFitness(fitnessValue);

		return foodSource;
	}
	
	public void sendScoutBees(Statistic statistic, int round) {
		int maxTrialIndex = 0;
		Individual foodMaxTrial;
		for (int indexFood = 1; indexFood < this.foodSources.size(); indexFood++) {
			foodMaxTrial = this.foodSources.get(maxTrialIndex);
			Individual food = this.foodSources.get(indexFood);
			if (food.getTrial() > foodMaxTrial.getTrial())
				maxTrialIndex = indexFood;
		}

		foodMaxTrial = this.foodSources.get(maxTrialIndex);
		if (foodMaxTrial.getTrial() >= ABCProperties.MAX_TRIAL) {
			this.foodSources.initializeIndividual(maxTrialIndex);

			statistic.verifyEvaluationInstant(round, this.foodSources);
		}
	}	
}
