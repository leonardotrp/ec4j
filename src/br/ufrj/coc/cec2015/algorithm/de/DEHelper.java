package br.ufrj.coc.cec2015.algorithm.de;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import br.ufrj.coc.cec2015.algorithm.Individual;
import br.ufrj.coc.cec2015.algorithm.Population;
import br.ufrj.coc.cec2015.algorithm.de.DEProperties.Strategy;
import br.ufrj.coc.cec2015.util.Helper;
import br.ufrj.coc.cec2015.util.Properties;

public class DEHelper {
	protected static List<Integer> populationIndexes = new ArrayList<Integer>(Properties.POPULATION_SIZE); // lista com os �ndices da popula��o, para nunca selecionar um �ndice repetido desnecessariamente
	static void initializePopulationIndexes() {
		populationIndexes.clear();
		for (int index = 0; index < Properties.POPULATION_SIZE; index++) {
			populationIndexes.add(index);
		}
	}
	static int randomPopulationIndex() {
		int index = Helper.randomInRange(0, populationIndexes.size() - 1);
		int populationIndex = populationIndexes.get(index);
		populationIndexes.remove(index);
		return populationIndex;
	}
	protected static void removePopulationIndex(int populationIndex) {
		if (populationIndexes.contains(populationIndex)) {
			int index = populationIndexes.indexOf(populationIndex);
			populationIndexes.remove(index);
		}
	}
	
	private static double[] CUMULATIVE_VECTOR;
	static {
		CUMULATIVE_VECTOR = new double[Properties.POPULATION_SIZE];
		CUMULATIVE_VECTOR[0] = Properties.POPULATION_SIZE;
		CUMULATIVE_VECTOR[1] = CUMULATIVE_VECTOR[0] + CUMULATIVE_VECTOR[0] * 0.8;
		for (int index = 2; index < Properties.POPULATION_SIZE; index++) {
			CUMULATIVE_VECTOR[index] = CUMULATIVE_VECTOR[index - 1] + (CUMULATIVE_VECTOR[index - 1] - CUMULATIVE_VECTOR[index - 2]) * 0.8;
		}
	}
	
	public static boolean canCrossover(Individual current) {
		boolean rouletteAll = DEProperties.STRATEGY.equals(Strategy.RE_ALL);
		double crossoverRate = rouletteAll ? Helper.randomInRange(current.getCrossoverRate(), 1.0) : current.getCrossoverRate(); 
		return Helper.randomInRange(0.0, 1.0) <= crossoverRate;
	}
	
	static Population sortedPopulation = null;
	private static void initializeSortedPopulation(Population population) {
		try {
			sortedPopulation = (Population) population.clone();
			Collections.sort(sortedPopulation.getIndividuals());
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	private static int rouletteWheel() {
		double pin = Helper.randomInRange(0.0, 1.0) * CUMULATIVE_VECTOR[sortedPopulation.size() - 1];
		int indexResult = 0;
		for (int index = 0; index < Properties.POPULATION_SIZE; index++) {
			if (CUMULATIVE_VECTOR[index] >= pin) {
				indexResult = index;
				break;
			}
		}
		return indexResult;
	}
	
	private static Individual spinRoulette() {
		int drwanIndex = rouletteWheel();
		return sortedPopulation.remove(drwanIndex);
	}
	
	private static boolean isCurrentToStrategy() {
		return DEProperties.STRATEGY.equals(Strategy.CURRENT_TO_BEST) || DEProperties.STRATEGY.equals(Strategy.CURRENT_TO_RAND) || isJADE();
	}
	
	private static boolean isBestStrategy() {
		return DEProperties.STRATEGY.equals(Strategy.BEST)/* || DEProperties.STRATEGY.equals(Strategy.BEST_2_OPT)*/;
	}
	
	private static boolean isRandStrategy() {
		return DEProperties.STRATEGY.equals(Strategy.RAND)/* || DEProperties.STRATEGY.equals(Strategy.RAND_2_OPT)*/;
	}

	private static boolean isRouletteStrategy() {
		return DEProperties.STRATEGY.equals(Strategy.RE_BASE) || isRouletteStrategyOthers();
	}
	
	private static boolean isRouletteStrategyOthers() {
		return DEProperties.STRATEGY.equals(Strategy.RE_ALL)/* || DEProperties.STRATEGY.equals(Strategy.RE_2_OPT)*/;
	}
	/*
	private static boolean isTwoOptMutation() {
		return DEProperties.STRATEGY.equals(Strategy.BEST_2_OPT) || DEProperties.STRATEGY.equals(Strategy.RAND_2_OPT) || DEProperties.STRATEGY.equals(Strategy.RE_2_OPT);
	}
	*/
	
	private static boolean isJADE() {
		return DEProperties.STRATEGY.equals(Strategy.CURRENT_TO_pBEST);
	}
	private static boolean isJADEWithArchieve() {
		return isJADE() && DEProperties.EXTERNAL_ARCHIVE;
	}
	private static boolean isEigenvectorCrossover() {
		return DEProperties.CROSSOVER.equals(DEProperties.Crossover.EIGENVECTOR);
	}
	
	private static Individual selectDestiny(Population population) {
		if (isCurrentToStrategy()) {
			if (DEProperties.STRATEGY.equals(Strategy.CURRENT_TO_BEST))
				return population.getBest();
			if (DEProperties.STRATEGY.equals(Strategy.CURRENT_TO_RAND)) {
				return population.get(randomPopulationIndex());
			}
			if (isJADE()) {
				BigDecimal p = new BigDecimal(DEProperties.GREEDINESS);
				//BigDecimal percentTop = p.multiply(BigDecimal.valueOf(100)); // 100 . p%
				int indexLastTop = p.multiply(BigDecimal.valueOf(Properties.INDIVIDUAL_SIZE)).intValue();
				int indexTop = indexLastTop == 0 ? 0 : Helper.randomInRange(0, indexLastTop);
				return sortedPopulation.get(indexTop);
			}
		}
		return null;
	}

	private static Individual selectBase(Population population, Individual current) {
		Individual base = null;
		if (isBestStrategy()) {
			base = population.getBest();
		}
		else if (isCurrentToStrategy()) {
			base = current;
		}
		else if (isRandStrategy()) {
			base = population.get(randomPopulationIndex());
		}
		else if (isRouletteStrategy()) {
			base = spinRoulette();
		}
		return base;
	}
	
	private static List<Individual> selectPartners(Population population) {
		boolean rouletteSelection = isRouletteStrategyOthers();
		List<Individual> partners = new ArrayList<Individual>();
		
		partners.add(rouletteSelection ? spinRoulette() : population.get(randomPopulationIndex())); // X1

		Individual X2 = isJADEWithArchieve() ? JADEHelper.randomFromArchieve() : population.get(randomPopulationIndex());
		partners.add(X2); // X2

		if (DEProperties.MUTATION_DIFFERENCES_COUNT > 1) {
			partners.add(rouletteSelection ? spinRoulette() : population.get(randomPopulationIndex())); // X3
			partners.add(rouletteSelection ? spinRoulette() : population.get(randomPopulationIndex())); // X4
		}

		return partners;
	}
	
	private static BigDecimal mutate(int j, double differencialWeight, Individual base, Individual destiny, List<Individual> partners) {
		BigDecimal xBaseG = new BigDecimal(base.get(j)), mutatedValue = xBaseG;
		BigDecimal F = new BigDecimal(differencialWeight), difference;
		
		if (destiny != null) { // 'DE/current-to-{rand/best/pbest}/N'
			BigDecimal K = DEProperties.STRATEGY.equals(Strategy.CURRENT_TO_RAND) ? new BigDecimal(Helper.randomInRange(0.0, 1.0)) : F;
			BigDecimal xDestiny = new BigDecimal(destiny.get(j));
			difference = xDestiny.subtract(xBaseG);
			mutatedValue = mutatedValue.add(K.multiply(difference));
		}

		int index = 0;
		while (index < partners.size()) {
			BigDecimal x1G = new BigDecimal(partners.get(index++).get(j));
			BigDecimal x2G = new BigDecimal(partners.get(index++).get(j));
			
			difference = x1G.subtract(x2G);
			mutatedValue = mutatedValue.add(F.multiply(difference));
		}
		
		return mutatedValue;
	}
	
	private static double getDifferencialWeight(Individual current) {
		boolean rouletteOthers = isRouletteStrategyOthers();
		return rouletteOthers ? Helper.randomInRange(current.getDifferencialWeight(), 1.0) : current.getDifferencialWeight();
	}
	
	public static double[] generateTrialVector(Population population, Individual current /* Xi,G */) {
		initializePopulationIndexes();
		initializeSortedPopulation(population);

		Individual base = selectBase(population, current); // XBase
		removePopulationIndex(population.indexOf(base));

		List<Individual> partners = selectPartners(population); // X1, X2, X3, X4
		Individual destiny = selectDestiny(population); // strategy 'DE/current-to-{rand/best/pbest}/N'
		
		boolean isEigXover = isEigenvectorCrossover() && Math.random() < 0.5;

		double differencialWeight = getDifferencialWeight(current);
		double[] trialVector = new double[Properties.INDIVIDUAL_SIZE]; /* Vi */
		for (int j = 0; j < Properties.INDIVIDUAL_SIZE; j++) {
			int randI = Helper.randomInRange(0, Properties.INDIVIDUAL_SIZE - 1);
			
			if (canCrossover(current) || j == randI) {
				trialVector[j] = mutate(j, differencialWeight, base, destiny, partners).doubleValue();
			}
			else {
				trialVector[j] = current.get(j); /* Xi */
			}
			
			if (isEigXover) {
				for (int k = 0; k < Properties.INDIVIDUAL_SIZE; k++) {
					trialVector[j] *= eigenDecomposition.getEntry(k, j); /* xover(Qg.Xi, Qg.Vi) */
				}
			}
		}

		if (isEigXover) {
			for (int j = 0; j < Properties.INDIVIDUAL_SIZE; j++) {
				for (int k = 0; k < Properties.INDIVIDUAL_SIZE; k++) {
					trialVector[j] *= eigenDecomposition.getEntry(j, k); /* Qg* . xover(Qg.Xi, Qg.Vi) */
				}
			}
		}
		
		return trialVector; // Ui,G+1
	}
/*
	private static double[] generateTrialVectorBin(Individual current, Individual base, Individual destiny, List<Individual> partners) {
		double differencialWeight = getDifferencialWeight(current);
		double[] trialVector = new double[Properties.INDIVIDUAL_SIZE]; /* Vi *
		for (int j = 0; j < Properties.INDIVIDUAL_SIZE; j++) {
			int randI = Helper.randomInRange(0, Properties.INDIVIDUAL_SIZE - 1);
			
			if (canCrossover(current) || j == randI) {
				trialVector[j] = mutate(j, differencialWeight, base, destiny, partners).doubleValue();
			}
			else {
				trialVector[j] = current.get(j);
			}
		}
		return trialVector; // Ui,G+1
	}

	private static double[] generateTrialVectorEig(Individual current, Individual base, Individual destiny, List<Individual> partners) {
		double differencialWeight = getDifferencialWeight(current);

		double[] X = current.getId();
		double[] V = new double[Properties.INDIVIDUAL_SIZE]; /* Vi *
		for (int j = 0; j < Properties.INDIVIDUAL_SIZE; j++) {
			V[j] = mutate(j, differencialWeight, base, destiny, partners).doubleValue();
		}

		double[] QX = new double[Properties.INDIVIDUAL_SIZE]; /* QX *
		double[] QV = new double[Properties.INDIVIDUAL_SIZE]; /* QX *
		for (int j = 0; j < Properties.INDIVIDUAL_SIZE; j++) {
			QX[j] = 0;
			QV[j] = 0;
			for (int k = 0; k < Properties.INDIVIDUAL_SIZE; k++) {
				QX[j] += eigenDecomposition.getEntry(j, k) * QX[j]; /* Qg.Xi *
				QV[j] += eigenDecomposition.getEntry(j, k) * QV[j]; /* Qg.Vi *
			}
		}

		double[] QU = new double[Properties.INDIVIDUAL_SIZE]; /* Vi *
		for (int j = 0; j < Properties.INDIVIDUAL_SIZE; j++) {
			int randI = Helper.randomInRange(0, Properties.INDIVIDUAL_SIZE - 1);
			if (canCrossover(current) || j == randI) {
				QU[j] = V[j];
			}
			else {
				QU[j] = X[j];
			}
		}

		double[] trialVector = new double[Properties.INDIVIDUAL_SIZE];
		for (int j = 0; j < Properties.INDIVIDUAL_SIZE; j++) {
			trialVector[j] = 0;
			for (int k = 0; k < Properties.INDIVIDUAL_SIZE; k++) {
				trialVector[j] += eigenDecomposition.getEntry(j, k) * QU[j]; /* Qg* . xover(Qg.Xi, Qg.Vi) *
			}
		}
		return trialVector; // Ui,G+1
	}
	
	public static double[] generateTrialVector(Population population, Individual current /* Xi,G *) {
		initializePopulationIndexes();
		initializeSortedPopulation(population);		
		
		Individual base = selectBase(population, current); // XBase
		removePopulationIndex(population.indexOf(base));

		List<Individual> partners = selectPartners(population); // X1, X2, X3, X4
		Individual destiny = selectDestiny(population); // strategy 'DE/current-to-{rand/best/pbest}/N'
		
		if (isEigenvectorCrossover() && Math.random() < 0.5)
			return generateTrialVectorEig(current, base, destiny, partners);
		else
			return generateTrialVectorBin(current, base, destiny, partners);
	}
*/
	private static RealMatrix getEigenDecomposition(Population population) {
		int D = Properties.INDIVIDUAL_SIZE;
		int NP = population.size();
		
		// Covariance matrix (12)
		double[][] C = new double[D][D];
		double[] m = new double[D];

		for (int j = 0; j < D; ++j) {
			m[j] = population.get(0).get(j);
		}

		for (int i = 1; i < NP; ++i) {
			for (int j = 0; j < D; ++j) {
				m[j] += population.get(i).get(j);
			}
		}

		for (int i = 0; i < D; ++i) {
			m[i] /= NP;
		}
		
		for (int i = 0; i < D; ++i) {
			for (int j = 0; j < D; ++j) {
				C[i][j] = 0;
				for (int k = 0; k < NP; ++k) {
					C[i][j] += (population.get(k).get(i) - m[i]) * (population.get(k).get(j) - m[j]);
				}
				C[i][j] /= (NP - 1);
			}
		}
		
		// Eigendecomposition (14)
		RealMatrix RM_C = new Array2DRowRealMatrix(C);
		RealMatrix RM_Q = new EigenDecomposition(RM_C).getV();
		return RM_Q;
	}

	// ======================================= JADE =======================================
	static RealMatrix eigenDecomposition;
	public static void initialize() {
		if (isJADE())
			JADEHelper.initialize();
		eigenDecomposition = null;
	}
	public static void initializeGeneration(Population population) {
		if (isJADE())
			JADEHelper.initializeGeneration(population);
		if (DEProperties.CROSSOVER.equals(DEProperties.Crossover.EIGENVECTOR)) {
			eigenDecomposition = getEigenDecomposition(population);
		}
	}
	public static void generateControlParameters(Individual individual) {
		if (isJADE()) {
			individual.setCrossoverRate(JADEHelper.generateCrossoverRate());
			individual.setDifferencialWeight(JADEHelper.generateDifferencialWeight());
		}
		else {
			individual.setCrossoverRate(DEProperties.CROSSOVER_RATE);
			individual.setDifferencialWeight(DEProperties.DIFFERENTIAL_WEIGHT);
		}
	}
	public static void addSuccessful(Individual successful) {
		if (isJADE())
			JADEHelper.addSuccessfulControlParameters(successful.getCrossoverRate(), successful.getDifferencialWeight());
	}
	public static void addInferior(Individual inferior) {
		if (isJADEWithArchieve())
			JADEHelper.addInferior(inferior);
	}
	public static void finalizeGeneration() {
		if (isJADE()) {
			JADEHelper.updateMeanCrossoverRate();
			JADEHelper.updateLocationDifferencialWeight();
		}
	}
}
