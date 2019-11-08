package br.ufrj.coc.cec2015.algorithm.jade;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import br.ufrj.coc.cec2015.algorithm.Individual;
import br.ufrj.coc.cec2015.algorithm.Population;
import br.ufrj.coc.cec2015.algorithm.de.DEHelper;
import br.ufrj.coc.cec2015.algorithm.de.DEProperties;
import br.ufrj.coc.cec2015.util.Helper;
import br.ufrj.coc.cec2015.util.Properties;
import br.ufrj.coc.cec2015.util.Statistic;

public class JADEHelper extends DEHelper {
	private double mean_CR, location_F;

	// Denote A as the set of archived inferior solutions and P as the current population.
	private List<Individual> inferiors = new ArrayList<>();
	// Denote Scr as the set of all successful crossover probabilities CRi’s at
	// generation g.
	private List<Double> successCrossoverRates = new ArrayList<>();
	// Denote SF as the set of all successful mutation factors in generation g.
	private List<Double> successDiffWeights = new ArrayList<>();

	private int countIncreases = 0;
	
	public JADEHelper() {
		super();
		this.initialize();
	}

	protected void initialize() {
		this.mean_CR = 0.5;
		this.location_F = 0.5;
		this.inferiors.clear(); // A = {}
		this.countIncreases = 0;
	}

	private void updateMeanCrossoverRate() {
		BigDecimal newMeanCR = new BigDecimal(1);
		newMeanCR = newMeanCR.subtract(new BigDecimal(DEProperties.ADAPTATION_RATE)).multiply(new BigDecimal(mean_CR));
		BigDecimal meanA = new BigDecimal(
				successCrossoverRates.isEmpty() ? 0.0 : Statistic.calculateMean(successCrossoverRates));
		meanA = meanA.multiply(new BigDecimal(DEProperties.ADAPTATION_RATE));
		newMeanCR = newMeanCR.add(meanA);
		mean_CR = newMeanCR.doubleValue();
	}

	private void updateLocationDifferencialWeight() {
		BigDecimal newLocationF = new BigDecimal(1);
		newLocationF = newLocationF.subtract(new BigDecimal(DEProperties.ADAPTATION_RATE))
				.multiply(new BigDecimal(location_F));
		BigDecimal meanL = new BigDecimal(
				successDiffWeights.isEmpty() ? 0.0 : Statistic.calculateLehmerMean(successDiffWeights));
		meanL = meanL.multiply(new BigDecimal(DEProperties.ADAPTATION_RATE));
		newLocationF = newLocationF.add(meanL);
		location_F = newLocationF.doubleValue();
	}

	public Individual getIndividualX2() {
		return super.getProperties().isJADEWithArchieve() ? this.randomFromArchieve() : super.getIndividualX2();
	}

	public void addInferior(Individual inferior) {
		if (super.getProperties().isJADEWithArchieve()) {
			if (inferiors.size() == super.getPopulation().size()) { // Randomly remove solutions from A so that |A| <=
																	// NP
				int indexToRemove = Helper.randomInRange(0, super.getPopulation().size() - 1);
				inferiors.remove(indexToRemove);
			}
			if (!inferiors.contains(inferior)) {
				inferiors.add(inferior);
			}
		}
	}

	public void initializeGeneration(Population currentPopulation) {
		super.initializeGeneration(currentPopulation);
		this.successCrossoverRates.clear(); // Scr = {}
		this.successDiffWeights.clear(); // Sf = {}
	}

	protected void increasePopulation(Population population, double determinant) {
		boolean useIncreasePopulation = DEProperties.MAX_INCREASE_POPULATION_WITH_EIG > 0;
		boolean canIncrease = this.countIncreases < DEProperties.MAX_INCREASE_POPULATION_WITH_EIG;
		if (useIncreasePopulation && canIncrease && this.testLimitFactorMaxFES()) {
			//System.out.println(String.format("Percentual de MaxFES = %.2f / Determinant = %e / Difference = %e", Properties.ARGUMENTS.get().getEvolutionPercentage(), determinant, Math.abs(determinant - population.getDeterminant())));
			boolean limitDetG = (Math.abs(determinant - population.getDeterminant()) < DEProperties.LIMIT_VARIANCE_DET_COVMATRIX);
			population.setDeterminant(determinant);
			if (limitDetG) {
				// increase population by keeping better pBest individuals
				int newSize = (int) (population.size() * 2);
				this.increase(population, newSize, this.selectPBestIndex());
				System.err.println("Increase population to " + population.size());
				this.initializeGeneration(population);
			}
		}
	}

	protected boolean testLimitFactorMaxFES() {
		int factorExtended = this.countIncreases == 0 ? 1 : this.countIncreases;
		return Properties.ARGUMENTS.get().getEvolutionPercentage() <= DEProperties.LIMIT_FACTOR_MAXFES_WITH_EIG * factorExtended;
	}
	
	private void increase(Population population, int newSize, int pBestIndex) {
		super.initializeSortedPopulation();
		if (newSize > population.size()) {
			// initialize
			Population sortedPopulation = super.getSortedPopulation();
			for (int index = pBestIndex + 1; index < sortedPopulation.size(); index++)
				sortedPopulation.initializeIndividual(index);
			
			// increase
			int increaseSize = newSize - population.size();
			for (int index = 0; index < increaseSize; index++)
				population.addIndividual();

			Properties.ARGUMENTS.get().setPopulationSize(newSize);
			this.countIncreases++;
		}
	}
	
	public Individual selectDestiny() {
		int indexTop = selectPBestIndex();
		return super.getSortedPopulation().get(indexTop);
	}

	private int selectPBestIndex() {
		BigDecimal p = new BigDecimal(DEProperties.GREEDINESS);
		// BigDecimal percentTop = p.multiply(BigDecimal.valueOf(100)); // 100 . p%
		int indexLastTop = p.multiply(BigDecimal.valueOf(Properties.ARGUMENTS.get().getPopulationSize())).intValue();
		int indexTop = indexLastTop == 0 ? 0 : Helper.randomInRange(0, indexLastTop);
		return indexTop;
	}

	public void generateControlParameters(Individual individual) {
		individual.setCrossoverRate(super.generateCrossoverRate(this.mean_CR, 0.0, 1.0));
		individual.setDifferencialWeight(super.generateDifferencialWeight(this.location_F, 0.0, 1.0));
	}

	public void addSuccessful(Individual successful) {
		this.successCrossoverRates.add(successful.getCrossoverRate());
		this.successDiffWeights.add(successful.getDifferencialWeight());
	}

	public void finalizeGeneration() {
		this.updateMeanCrossoverRate();
		this.updateLocationDifferencialWeight();
	}

	// Randomly choose ˜xr2,g <> xr1,g <> xi,g from P union A
	private Individual randomFromArchieve() {
		List<Individual> unionPA = new ArrayList<>(super.getPopulationIndexes().size() + this.inferiors.size());
		for (Integer populationIndex : super.getPopulationIndexes()) {
			unionPA.add(super.getPopulation().get(populationIndex));
		}
		unionPA.addAll(this.inferiors);
		int randomUnionPAIndex = Helper.randomInRange(0, unionPA.size() - 1);

		Individual individual = unionPA.get(randomUnionPAIndex);

		int index = super.getPopulation().indexOf(individual);
		super.removePopulationIndex(index);

		return individual;
	}
}