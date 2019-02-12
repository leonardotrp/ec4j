package br.ufrj.coc.cec2015.algorithm.de;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.distribution.CauchyDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;

import br.ufrj.coc.cec2015.algorithm.Individual;
import br.ufrj.coc.cec2015.algorithm.Population;
import br.ufrj.coc.cec2015.util.Helper;
import br.ufrj.coc.cec2015.util.Statistic;

public class JADEFunctions {
	private double mean_CR, location_F;

	private Population population;
	// Denote A as the set of archived inferior solutions and P as the current population.
	private List<Individual> inferiors = new ArrayList<>();
	// Denote Scr as the set of all successful crossover probabilities CRi’s at generation g.
	private List<Double> successCrossoverRates = new ArrayList<>();
	// Denote SF as the set of all successful mutation factors in generation g.
	private List<Double> successDiffWeights = new ArrayList<>();
	
	public JADEFunctions() {
		super();
	}
	
	public List<Individual> getInferiors() {
		return this.inferiors;
	}

	protected void addInferior(Individual inferior) {
		if (inferiors.size() == this.population.size()) { // Randomly remove solutions from A so that |A| <= NP
			int indexToRemove = Helper.randomInRange(0, this.population.size() - 1);
			inferiors.remove(indexToRemove);
		}
		if (!inferiors.contains(inferior)) {
			inferiors.add(inferior);
		}
	}

	protected void initialize() {
		mean_CR = 0.5;
		location_F = 0.5;
		inferiors.clear(); // A = {}
	}

	protected void initializeGeneration(Population currentPopulation) {
		population = currentPopulation;
		successCrossoverRates.clear(); // Scr = {}
		successDiffWeights.clear(); // Sf = {}
	}

	protected void addSuccessfulControlParameters(double crossoverRate, double differencialWeight) {
		successCrossoverRates.add(crossoverRate);
		successDiffWeights.add(differencialWeight);
	}

	/**
	 * At each generation g, the crossover probability CRi of each individual xi is independently generated according to a normal
	 * distribution of mean μCR and standard deviation 0.1
	 * @return double
	 */
	protected double generateCrossoverRate() {
		double standardDeviation = 0.1;
		// generate crossover rate to individual
		NormalDistribution d = new NormalDistribution(mean_CR, standardDeviation);
		double cr = d.sample();
		return (cr < 0.0) ? 0.0 : (cr > 1.0) ? 1.0 : cr;
	}

	/**
	 * At each generation g, the mutation factor Fi of each individual xi is independently generated according to
	 * a Cauchy distribution with location parameter μF and scale parameter 0.1
	 * @return double
	 */
	protected double generateDifferencialWeight() {
		double scale = 0.1;
		CauchyDistribution c = new CauchyDistribution(location_F, scale);
		double f = c.sample();
		return (f >= 1.0) ? 1.0 : (f <= 0) ? generateDifferencialWeight() : f;
	}

	protected void updateMeanCrossoverRate() {
		BigDecimal newMeanCR = new BigDecimal(1);
		newMeanCR = newMeanCR.subtract(new BigDecimal(DEProperties.ADAPTATION_RATE)).multiply(new BigDecimal(mean_CR));
		BigDecimal meanA = new BigDecimal(successCrossoverRates.isEmpty() ? 0.0 : Statistic.calculateMean(successCrossoverRates));
		meanA = meanA.multiply(new BigDecimal(DEProperties.ADAPTATION_RATE));
		newMeanCR = newMeanCR.add(meanA);
		mean_CR = newMeanCR.doubleValue();
	}

	protected void updateLocationDifferencialWeight() {
		BigDecimal newLocationF = new BigDecimal(1);
		newLocationF = newLocationF.subtract(new BigDecimal(DEProperties.ADAPTATION_RATE)).multiply(new BigDecimal(location_F));
		BigDecimal meanL = new BigDecimal(successDiffWeights.isEmpty() ? 0.0 : Statistic.calculateLehmerMean(successDiffWeights));
		meanL = meanL.multiply(new BigDecimal(DEProperties.ADAPTATION_RATE));
		newLocationF = newLocationF.add(meanL);
		location_F = newLocationF.doubleValue();
	}
}