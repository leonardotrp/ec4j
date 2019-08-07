package br.ufrj.coc.cec2015.algorithm.dpade;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import br.ufrj.coc.cec2015.algorithm.Individual;
import br.ufrj.coc.cec2015.algorithm.Population;
import br.ufrj.coc.cec2015.algorithm.de.DEProperties;
import br.ufrj.coc.cec2015.algorithm.jade.JADEHelper;
import br.ufrj.coc.cec2015.util.Helper;
import br.ufrj.coc.cec2015.util.Statistic;

public class DPADEHelper extends JADEHelper {
	private static double MIN_PROBABILITY = 0.1; // Pmin
	private static double EPSILON = 0.0001;

	private double F_low = 0, F_high = 1, F_medium = (F_low + F_high) / 2.0;
	private double CR_low = 0, CR_high = 1, CR_medium = (CR_low + CR_high) / 2.0;

	private double propability_F1 = 0.5, propability_F2 = 0.5;
	private double mean_F1 = (F_low + F_medium) / 2.0, mean_F2 = (F_medium + F_high) / 2.0;
	
	private double propability_CR1 = 0.5, propability_CR2 = 0.5;
	private double mean_CR1 = (CR_low + CR_medium) / 2.0, mean_CR2 = (CR_medium + CR_high) / 2.0;

	// Denote SF as the set of all successful mutation factors in generation g.
	private List<Double> successF1 = new ArrayList<>(), successF2 = new ArrayList<>();
	private List<Double> improvedF1 = new ArrayList<>(), improvedF2 = new ArrayList<>();
	// Denote SCR as the set of all successful crossover rates in generation g.
	private List<Double> successCR1 = new ArrayList<>(), successCR2 = new ArrayList<>();
	private List<Double> improvedCR1 = new ArrayList<>(), improvedCR2 = new ArrayList<>();
	
	public DPADEHelper() {
		super();
	}
	
	@Override
	protected void initialize() {
		super.initialize();
		this.F_low = 0; this.F_high = 1; this.F_medium = (F_low + F_high) / 2.0;
		this.CR_low = 0; this.CR_high = 1; this.CR_medium = (CR_low + CR_high) / 2.0;

		this.propability_F1 = 0.5; this.propability_F2 = 0.5;
		this.mean_F1 = (F_low + F_medium) / 2.0; this.mean_F2 = (F_medium + F_high) / 2.0;
		
		this.propability_CR1 = 0.5; this.propability_CR2 = 0.5;
		this.mean_CR1 = (CR_low + CR_medium) / 2.0; mean_CR2 = (CR_medium + CR_high) / 2.0;
	}

	/**
	 * Randomly select |PF1 . NP| individuals whose F_flag is set to 1 (false), and F_flag of the remainder individuals are set to 2 (true)
	 */
	private void selectIndividualsByF() {
		int countLeft = (int) (this.propability_F1 * super.getPopulation().size());
		List<Individual> individuals = new ArrayList<Individual>(super.getPopulation().getIndividuals());
		while (countLeft-- < 0) {
			int index = Helper.randomInRange(0, individuals.size());
			individuals.get(index).setF_flag(false);
			individuals.remove(index);
		}
		individuals.forEach(i -> i.setF_flag(true));
	}

	/**
	 * Randomly select |PCR1 . NP| individuals whose CR_flag is set to 1 (false), and CR_flag of the remainder individuals are set to 2 (true)
	 */
	private void selectIndividualsByCR() {
		int countLeft = (int) (this.propability_CR1 * super.getPopulation().size());
		List<Individual> individuals = new ArrayList<Individual>(super.getPopulation().getIndividuals());
		while (countLeft-- < 0) {
			int index = Helper.randomInRange(0, individuals.size());
			individuals.get(index).setCR_flag(false);
			individuals.remove(index);
		}
		individuals.forEach(i -> i.setCR_flag(true));
	}
	
	@Override
	public void initializeGeneration(Population population) {
		super.initializeGeneration(population);
		this.successF1.clear(); this.successF2.clear();
		this.improvedF1.clear(); this.improvedF2.clear();
		this.successCR1.clear(); this.successCR2.clear();
		this.improvedCR1.clear(); this.improvedCR2.clear();
		selectIndividualsByF();
		selectIndividualsByCR();
	}

	public void generateControlParameters(Individual individual) {
		individual.setCrossoverRate(super.generateCrossoverRate(individual.isCR_flag() ? this.mean_CR1 : this.mean_CR2));
		individual.setDifferencialWeight(super.generateDifferencialWeight(individual.isF_flag() ? this.mean_F1 : this.mean_F2));
	}

	public void addSuccessful(Individual successful) {
		if (successful.isCR_flag()) {
			this.successCR1.add(successful.getCrossoverRate());
			this.improvedCR1.add(successful.getImprovedFunctionValue());
		}
		else {
			this.successCR2.add(successful.getCrossoverRate());
			this.improvedCR2.add(successful.getImprovedFunctionValue());
		}
		if (successful.isF_flag()) {
			this.successF1.add(successful.getDifferencialWeight());
			this.improvedF1.add(successful.getImprovedFunctionValue());
		}
		else {
			this.successF2.add(successful.getDifferencialWeight());
			this.improvedF2.add(successful.getImprovedFunctionValue());
		}
	}
	
	/**
	 * Equations (15) e (16) ... Statistic.calculateWeightedMean (17) e (18)
	 * @param meanProperty
	 * @param successControlParameters
	 * @param improvedFunctionValues
	 * @return double
	 */
	private double calculateNewMeanProperty(double meanProperty, List<Double> successControlParameters, List<Double> improvedFunctionValues) {
		BigDecimal newMeanCR = new BigDecimal(1);
		newMeanCR = newMeanCR.subtract(new BigDecimal(DEProperties.ADAPTATION_RATE)).multiply(new BigDecimal(meanProperty));
		BigDecimal meanA = new BigDecimal(successControlParameters.isEmpty() ? 0.0 : Statistic.calculateWeightedMean(successControlParameters, improvedFunctionValues));
		meanA = meanA.multiply(new BigDecimal(DEProperties.ADAPTATION_RATE));
		newMeanCR = newMeanCR.add(meanA);
		return newMeanCR.doubleValue();
	}
	
	private boolean hasSuccessControlParameters() {
		return !this.successCR1.isEmpty() && !this.successCR2.isEmpty() && !this.successF1.isEmpty() && !this.successF2.isEmpty();
	}
	
	class SuccessRates {
		double sr_F1;
		double sr_F2;
		double sr_CR1;
		double sr_CR2;
		public SuccessRates(double sr_F1, double sr_F2, double sr_CR1, double sr_CR2) {
			super();
			this.sr_F1 = sr_F1;
			this.sr_F2 = sr_F2;
			this.sr_CR1 = sr_CR1;
			this.sr_CR2 = sr_CR2;
		}
		public double getF1Factor() {
			return this.sr_F1 / (this.sr_F1 + this.sr_F2);
		}
		public double getF2Factor() {
			return this.sr_F2 / (this.sr_F1 + this.sr_F2);
		}
		public double getCR1Factor() {
			return this.sr_CR1 / (this.sr_CR1 + this.sr_CR2);
		}
		public double getCR2Factor() {
			return this.sr_CR2 / (this.sr_CR1 + this.sr_CR2);
		}
	}

	/**
	 * Equations (20) e (21)
	 */
	private SuccessRates calculateSuccessfulRates() {
		Stream<Individual> streamIndividuals = this.getPopulation().getIndividuals().stream();
		
		// SR_F1, SR_F2
		long count_F1 = streamIndividuals.filter(i -> !i.isF_flag()).count();
		double sr_F1 = (this.successF1.size() / count_F1) * EPSILON; 
		long count_F2 = Math.abs(this.getPopulation().size() - count_F1);
		double sr_F2 = (this.successF2.size() / count_F2) * EPSILON; 
		
		// SR_CR1, SR_CR2
		long count_CR1 = streamIndividuals.filter(i -> !i.isCR_flag()).count();
		double sr_CR1 = (this.successF1.size() / count_CR1) * EPSILON; 
		long count_CR2 = Math.abs(this.getPopulation().size() - count_F1);
		double sr_CR2 = (this.successF2.size() / count_CR2) * EPSILON;
		
		return new SuccessRates(sr_F1, sr_F2, sr_CR1, sr_CR2);
	}

	/**
	 * Equations (19)
	 * @param probalitityProperty
	 * @param successControlParameters
	 * @param improvedFunctionValues
	 * @return double
	 */
	private double calculateNewProbability(double probability, double successRateTerm) {
		BigDecimal newProbability = new BigDecimal(1);
		newProbability = newProbability.subtract(new BigDecimal(DEProperties.ADAPTATION_RATE)).multiply(new BigDecimal(probability));
		BigDecimal srTerm = new BigDecimal(successRateTerm);
		srTerm = srTerm.multiply(new BigDecimal(DEProperties.ADAPTATION_RATE));
		newProbability = newProbability.add(srTerm);
		return newProbability.doubleValue();
	}
	
	private void updateProbabilities() {
		SuccessRates successRates = calculateSuccessfulRates();
		this.propability_F1 = calculateNewProbability(this.propability_F1, successRates.getF1Factor()); // Algorithm 1: line 28
		if (this.propability_F1 < MIN_PROBABILITY) {
			this.F_low = this.F_medium;
			this.F_medium = (this.F_low + this.F_high) / 2;
			this.propability_F1 = 0.5;
			this.propability_F2 = 0.5;
		}		
		this.propability_F2 = calculateNewProbability(this.propability_F2, successRates.getF2Factor()); // Algorithm 1: line 29
		if (this.propability_F2 < MIN_PROBABILITY) {
			this.F_high = this.F_medium;
			this.F_medium = (this.F_low + this.F_high) / 2;
			this.propability_F1 = 0.5;
			this.propability_F2 = 0.5;
		}
		this.propability_CR1 = calculateNewProbability(this.propability_CR1, successRates.getCR1Factor()); // Algorithm 1: line 30
		if (this.propability_CR1 < MIN_PROBABILITY) {
			this.CR_low = this.CR_medium;
			this.CR_medium = (this.CR_low + this.CR_high) / 2;
			this.propability_CR1 = 0.5;
			this.propability_CR2 = 0.5;
		}
		this.propability_CR2 = calculateNewProbability(this.propability_CR2, successRates.getCR2Factor()); // Algorithm 1: line 31
		if (this.propability_CR2 < MIN_PROBABILITY) {
			this.CR_high = this.CR_medium;
			this.CR_medium = (this.CR_low + this.CR_high) / 2;
			this.propability_CR1 = 0.5;
			this.propability_CR2 = 0.5;
		}
	}
	
	public void finalizeGeneration() {
		if (hasSuccessControlParameters()) {  // Algorithm 1: lines 22, 23
			this.mean_CR1 = calculateNewMeanProperty(this.mean_CR1, this.successCR1, this.improvedCR1);
			this.mean_CR2 = calculateNewMeanProperty(this.mean_CR2, this.successCR2, this.improvedCR2);
			this.mean_F1 = calculateNewMeanProperty(this.mean_F1, this.successF1, this.improvedF1);
			this.mean_F2 = calculateNewMeanProperty(this.mean_F2, this.successF2, this.improvedF2);
		}
		updateProbabilities();// Algorithm 1: line 27
	}
}
