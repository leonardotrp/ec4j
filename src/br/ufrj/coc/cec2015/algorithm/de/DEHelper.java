package br.ufrj.coc.cec2015.algorithm.de;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import br.ufrj.coc.cec2015.algorithm.AlgorithmHelper;
import br.ufrj.coc.cec2015.algorithm.Individual;
import br.ufrj.coc.cec2015.algorithm.Population;
import br.ufrj.coc.cec2015.math.MatrixUtil;
import br.ufrj.coc.cec2015.util.Helper;
import br.ufrj.coc.cec2015.util.Properties;

public class DEHelper implements AlgorithmHelper {
	private Population population;
	private Population sortedPopulation;
	private double[] cumulativeVector;
	private List<Integer> populationIndexes;
	private DEProperties properties;

	public DEHelper() {
		super();
		this.properties = new DEProperties();
		initialize();
	}

	public void initializeGeneration(Population population) {
		this.population = population;
		this.initializeCumulativeVector();
		this.initializePopulationIndexes();
		this.initializeGeneration();
	}
	
	public DEProperties getProperties() {
		return this.properties;
	}
	
	// --------------------------------------------------------------------------------
	// --------- CUMULATIVE VECTOR ----------------------------------------------------
	// --------------------------------------------------------------------------------
	private void initializeCumulativeVector() {
		cumulativeVector = new double[this.population.size()];
		cumulativeVector[0] = this.population.size();
		cumulativeVector[1] = cumulativeVector[0] + cumulativeVector[0] * 0.8;
		for (int index = 2; index < this.population.size(); index++) {
			cumulativeVector[index] = cumulativeVector[index - 1] + (cumulativeVector[index - 1] - cumulativeVector[index - 2]) * 0.8;
		}
	}
	
	// --------------------------------------------------------------------------------
	// --------- POPULATION INDEXES ---------------------------------------------------
	// --------------------------------------------------------------------------------
	private void initializePopulationIndexes() {
		if (this.populationIndexes == null || this.populationIndexes.size() != this.population.size())
			this.populationIndexes = new ArrayList<Integer>(this.population.size()); // lista com os índices da população, para nunca selecionar um índice repetido desnecessariamente
		this.populationIndexes.clear();
		for (int index = 0; index < this.population.size(); index++) {
			this.populationIndexes.add(index);
		}
	}
	private int randomPopulationIndex() {
		int index = Helper.randomInRange(0, this.populationIndexes.size() - 1);
		int populationIndex = this.populationIndexes.get(index);
		this.populationIndexes.remove(index);
		return populationIndex;
	}
	private void removePopulationIndex(int populationIndex) {
		if (this.populationIndexes.contains(populationIndex)) {
			int index = this.populationIndexes.indexOf(populationIndex);
			this.populationIndexes.remove(index);
		}
	}
	public boolean canCrossover(Individual current) {
		boolean rouletteAll = this.properties.isRouletteAllStrategy();
		double crossoverRate = rouletteAll ? Helper.randomInRange(current.getCrossoverRate(), 1.0) : current.getCrossoverRate(); 
		return Math.random() <= crossoverRate;
	}
	private void initializeSortedPopulation() {
		try {
			this.sortedPopulation = (Population) this.population.clone();
			Collections.sort(this.sortedPopulation.getIndividuals());
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
	private int rouletteWheel() {
		double pin = Helper.randomInRange(0.0, 1.0) * this.cumulativeVector[this.sortedPopulation.size() - 1];
		int indexResult = 0;
		for (int index = 0; index < this.population.size(); index++) {
			if (this.cumulativeVector[index] >= pin) {
				indexResult = index;
				break;
			}
		}
		return indexResult;
	}
	private Individual spinRoulette() {
		int drwanIndex = rouletteWheel();
		return this.sortedPopulation.remove(drwanIndex);
	}
	private Individual selectDestiny() {
		if (this.properties.isCurrentToStrategy()) {
			if (this.properties.isCurrentToBestStrategy())
				return population.getBest();
			else if (this.properties.isCurrentToRandStrategy())
				return population.get(randomPopulationIndex());
			else if (this.properties.isJADE()) {
				BigDecimal p = new BigDecimal(DEProperties.GREEDINESS);
				//BigDecimal percentTop = p.multiply(BigDecimal.valueOf(100)); // 100 . p%
				int indexLastTop = p.multiply(BigDecimal.valueOf(Properties.ARGUMENTS.get().getIndividualSize())).intValue();
				int indexTop = indexLastTop == 0 ? 0 : Helper.randomInRange(0, indexLastTop);
				return this.sortedPopulation.get(indexTop);
			}
		}
		return null;
	}
	private Individual selectBase(Individual current) {
		Individual base = null;
		if (this.properties.isBestStrategy()) {
			base = this.population.getBest();
		}
		else if (this.properties.isCurrentToStrategy()) {
			base = current;
		}
		else if (this.properties.isRandStrategy()) {
			base = this.population.get(randomPopulationIndex());
		}
		else if (this.properties.isRouletteStrategy()) {
			base = spinRoulette();
		}
		return base;
	}
	private List<Individual> selectPartners() {
		boolean rouletteSelection = this.properties.isRouletteStrategyOthers();
		List<Individual> partners = new ArrayList<Individual>();
		
		partners.add(rouletteSelection ? spinRoulette() : this.population.get(randomPopulationIndex())); // X1

		Individual X2 = this.properties.isJADEWithArchieve() ? this.randomFromArchieve() : this.population.get(randomPopulationIndex());
		partners.add(X2); // X2

		if (this.properties.getMutationDifferenceCount() > 1) {
			partners.add(rouletteSelection ? spinRoulette() : population.get(randomPopulationIndex())); // X3
			partners.add(rouletteSelection ? spinRoulette() : population.get(randomPopulationIndex())); // X4
		}

		return partners;
	}
	private BigDecimal mutate(int j, double differencialWeight, Individual base, Individual destiny, List<Individual> partners) {
		BigDecimal xBaseG = new BigDecimal(base.get(j)), mutatedValue = xBaseG;
		BigDecimal F = new BigDecimal(differencialWeight), difference;
		
		if (destiny != null) { // 'DE/current-to-{rand/best/pbest}/N'
			BigDecimal K = this.properties.isCurrentToRandStrategy() ? new BigDecimal(Helper.randomInRange(0.0, 1.0)) : F;
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
	private double getDifferencialWeight(Individual current) {
		boolean rouletteOthers = this.properties.isRouletteStrategyOthers();
		return rouletteOthers ? Helper.randomInRange(current.getDifferencialWeight(), 1.0) : current.getDifferencialWeight();
	}
	private double[] generateTrialVectorBin(Individual current, Individual base, Individual destiny, List<Individual> partners) {
		double differencialWeight = getDifferencialWeight(current);
		int individualSize = Properties.ARGUMENTS.get().getIndividualSize();
		double[] trialVector = new double[individualSize]; /* Vi */
		for (int j = 0; j < individualSize; j++) {
			int randI = Helper.randomInRange(0, individualSize - 1);
			
			if (canCrossover(current) || j == randI) {
				trialVector[j] = mutate(j, differencialWeight, base, destiny, partners).doubleValue();
			}
			else {
				trialVector[j] = current.get(j);
			}
		}
		return trialVector; // Ui,G+1
	}
	private double[] generateTrialVectorEig(Individual current, Individual base, Individual destiny, List<Individual> partners) {
		double differencialWeight = getDifferencialWeight(current);
		int individualSize = Properties.ARGUMENTS.get().getIndividualSize();

		double[] X = current.getId();
		double[] V = new double[individualSize]; /* Vi */
		for (int j = 0; j < individualSize; j++) {
			V[j] = mutate(j, differencialWeight, base, destiny, partners).doubleValue();
		}
		
		//System.out.println("\nX = " + Arrays.toString(X));
		//System.out.println("V = " + Arrays.toString(V));
		
		RealMatrix Q = eigenDecomposition.getV(); // Qg
		
		double[] QX = new double[individualSize];
		double[] QV = new double[individualSize];
		for (int j = 0; j < individualSize; j++) {
			QX[j] = 0;
			QV[j] = 0;
			for (int k = 0; k < individualSize; k++) {
				//System.out.println("QX["+j+"] = QX["+j+"] + (Q.getEntry("+k+", "+j+") * X["+k+"]) = " + QX[j] + " + (" + Q.getEntry(k, j) + " * " + X[k] + ") = " + (QX[j] + Q.getEntry(k, j) * X[k]));
				QX[j] += Q.getEntry(k, j) * X[k]; /* Qg.Xi */
				QV[j] += Q.getEntry(k, j) * V[k]; /* Qg.Vi */
				//System.out.println("Q.getEntry("+k+", "+j+") * V["+k+"] = " + Q.getEntry(k, j) + " * " + V[k] + " = " + Q.getEntry(k, j) * V[k]);
			}
		}
		
		//System.out.println("\nQX = " + Arrays.toString(QX));

		double[] QU = new double[individualSize]; /* xover(Qg.Xi, Qg.Vi) */
		for (int j = 0; j < individualSize; j++) {
			int randI = Helper.randomInRange(0, individualSize - 1);
			if (canCrossover(current) || j == randI) {
				QU[j] = QV[j];
			}
			else {
				QU[j] = QX[j];
			}
		}
		
		//System.out.println("\nQU = " + Arrays.toString(QU));

		RealMatrix QT = eigenDecomposition.getVT(); // Qg*
		
		double[] trialVector = new double[individualSize];
		for (int j = 0; j < individualSize; j++) {
			trialVector[j] = 0;
			for (int k = 0; k < individualSize; k++) {
				//System.out.println("trialVector["+j+"] = trialVector["+j+"] + (QT.getEntry("+k+", "+j+") * QU["+k+"]) = " + trialVector[j] + " + (" + QT.getEntry(k, j) + " * " + QU[k] + ") = " + (trialVector[j] + QT.getEntry(k, j) * QU[k]));
				trialVector[j] += QT.getEntry(k, j) * QU[k]; /* Qg* . xover(Qg.Xi, Qg.Vi) */
			}
		}
		//System.out.println("\ntrialVector = " + Arrays.toString(trialVector));
		return trialVector; // Ui,G+1
	}
	public double[] generateTrialVector(Individual current /* Xi,G */) {
		this.initializePopulationIndexes();
		initializeSortedPopulation();		
		
		Individual base = this.selectBase(current); // XBase
		removePopulationIndex(this.population.indexOf(base));

		List<Individual> partners = this.selectPartners(); // X1, X2, X3, X4
		Individual destiny = this.selectDestiny(); // strategy 'DE/current-to-{rand/best/pbest}/N'
		
		if (this.properties.isEigenvectorCrossover() && Math.random() <= DEProperties.EIG_RATE)
			return generateTrialVectorEig(current, base, destiny, partners);
		else
			return generateTrialVectorBin(current, base, destiny, partners);
	}
	// ======================================= JADE =======================================
	private EigenDecomposition eigenDecomposition;
	private JADEHelper jadeFunctions;
	private void initialize() {
		jadeFunctions = new JADEHelper();
		if (this.properties.isJADE())
			jadeFunctions.initialize();
		this.eigenDecomposition = null;
	}
	private void initializeGeneration() {
		if (this.properties.isJADE())
			jadeFunctions.initializeGeneration(this.population);
		if (this.properties.isEigenvectorCrossover()) {
			//RealMatrix covarianceMatrix = MatrixUtil.getCovarianceMatrix(this.population.toMatrix());
			eigenDecomposition = MatrixUtil.getEigenDecomposition(this.population);
		}
	}
	public void generateControlParameters(Individual individual) {
		if (this.properties.isJADE()) {
			individual.setCrossoverRate(jadeFunctions.generateCrossoverRate());
			individual.setDifferencialWeight(jadeFunctions.generateDifferencialWeight());
		}
		else {
			individual.setCrossoverRate(DEProperties.CROSSOVER_RATE);
			individual.setDifferencialWeight(DEProperties.DIFFERENTIAL_WEIGHT);
		}
	}
	public void addSuccessful(Individual successful) {
		if (this.properties.isJADE())
			jadeFunctions.addSuccessfulControlParameters(successful.getCrossoverRate(), successful.getDifferencialWeight());
	}
	public void addInferior(Individual inferior) {
		if (this.properties.isJADEWithArchieve())
			jadeFunctions.addInferior(inferior);
	}
	public void finalizeGeneration() {
		if (this.properties.isJADE()) {
			jadeFunctions.updateMeanCrossoverRate();
			jadeFunctions.updateLocationDifferencialWeight();
		}
	}
	// Randomly choose ˜xr2,g <> xr1,g <> xi,g from P union A
	private Individual randomFromArchieve() {
		List<Individual> unionPA = new ArrayList<>(this.populationIndexes.size() + this.jadeFunctions.getInferiors().size());
		for (Integer populationIndex : this.populationIndexes) {
			unionPA.add(this.population.get(populationIndex));
		}
		unionPA.addAll(this.jadeFunctions.getInferiors());
		int randomUnionPAIndex = Helper.randomInRange(0, unionPA.size() - 1);
		
		Individual individual = unionPA.get(randomUnionPAIndex);

		int index = population.indexOf(individual);
		this.removePopulationIndex(index);
				
		return individual;
	}
}
