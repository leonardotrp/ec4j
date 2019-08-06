package br.ufrj.coc.cec2015.algorithm.jade;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.distribution.CauchyDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;

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
	// Denote Scr as the set of all successful crossover probabilities CRi’s at generation g.
	private List<Double> successCrossoverRates = new ArrayList<>();
	// Denote SF as the set of all successful mutation factors in generation g.
	private List<Double> successDiffWeights = new ArrayList<>();
	
	public JADEHelper() {
		super();
		this.initialize();
	}

	protected void initialize() {
		this.mean_CR = 0.5;
		this.location_F = 0.5;
		this.inferiors.clear(); // A = {}
	}
	
	private List<Individual> getInferiors() {
		return this.inferiors;
	}

	/**
	 * At each generation g, the crossover probability CRi of each individual xi is independently generated according to a normal
	 * distribution of mean μCR and standard deviation 0.1
	 * @return double
	 */
	private double generateCrossoverRate() {
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
	private double generateDifferencialWeight() {
		double scale = 0.1;
		CauchyDistribution c = new CauchyDistribution(location_F, scale);
		double f = c.sample();
		return (f >= 1.0) ? 1.0 : (f <= 0) ? generateDifferencialWeight() : f;
	}

	private void updateMeanCrossoverRate() {
		BigDecimal newMeanCR = new BigDecimal(1);
		newMeanCR = newMeanCR.subtract(new BigDecimal(DEProperties.ADAPTATION_RATE)).multiply(new BigDecimal(mean_CR));
		BigDecimal meanA = new BigDecimal(successCrossoverRates.isEmpty() ? 0.0 : Statistic.calculateMean(successCrossoverRates));
		meanA = meanA.multiply(new BigDecimal(DEProperties.ADAPTATION_RATE));
		newMeanCR = newMeanCR.add(meanA);
		mean_CR = newMeanCR.doubleValue();
	}

	private void updateLocationDifferencialWeight() {
		BigDecimal newLocationF = new BigDecimal(1);
		newLocationF = newLocationF.subtract(new BigDecimal(DEProperties.ADAPTATION_RATE)).multiply(new BigDecimal(location_F));
		BigDecimal meanL = new BigDecimal(successDiffWeights.isEmpty() ? 0.0 : Statistic.calculateLehmerMean(successDiffWeights));
		meanL = meanL.multiply(new BigDecimal(DEProperties.ADAPTATION_RATE));
		newLocationF = newLocationF.add(meanL);
		location_F = newLocationF.doubleValue();
	}
	
	public Individual getIndividualX2() {
		return super.getProperties().isJADEWithArchieve() ? this.randomFromArchieve() : super.getIndividualX2();
	}

	public void addInferior(Individual inferior) {
		if (super.getProperties().isJADEWithArchieve()) {
			if (inferiors.size() == super.getPopulation().size()) { // Randomly remove solutions from A so that |A| <= NP
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
	
	public Individual selectDestiny() {
		BigDecimal p = new BigDecimal(DEProperties.GREEDINESS);
		//BigDecimal percentTop = p.multiply(BigDecimal.valueOf(100)); // 100 . p%
		int indexLastTop = p.multiply(BigDecimal.valueOf(Properties.ARGUMENTS.get().getIndividualSize())).intValue();
		int indexTop = indexLastTop == 0 ? 0 : Helper.randomInRange(0, indexLastTop);
		return super.getSortedPopulation().get(indexTop);
	}
	
	public void generateControlParameters(Individual individual) {
		individual.setCrossoverRate(this.generateCrossoverRate());
		individual.setDifferencialWeight(this.generateDifferencialWeight());
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
		List<Individual> unionPA = new ArrayList<>(super.getPopulationIndexes().size() + this.getInferiors().size());
		for (Integer populationIndex : super.getPopulationIndexes()) {
			unionPA.add(super.getPopulation().get(populationIndex));
		}
		unionPA.addAll(this.getInferiors());
		int randomUnionPAIndex = Helper.randomInRange(0, unionPA.size() - 1);
		
		Individual individual = unionPA.get(randomUnionPAIndex);

		int index = super.getPopulation().indexOf(individual);
		super.removePopulationIndex(index);
				
		return individual;
	}
}