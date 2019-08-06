package br.ufrj.coc.cec2015.algorithm.dpade;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
	private double location_F1 = (F_low + F_medium) / 2.0, location_F2 = (F_medium + F_high) / 2.0;
	
	private double propability_CR1 = 0.5, propability_CR2 = 0.5;
	private double mean_CR1 = (CR_low + CR_medium) / 2.0, mean_CR2 = (CR_medium + CR_high) / 2.0;

	// Denote SF as the set of all successful mutation factors in generation g.
	private List<Double> successF1 = new ArrayList<>(), successF2 = new ArrayList<>();
	// Denote SCR as the set of all successful crossover rates in generation g.
	private List<Double> successCR1 = new ArrayList<>(), successCR2 = new ArrayList<>();
	
	public DPADEHelper() {
		super();
	}
	
	@Override
	protected void initialize() {
		super.initialize();
		this.F_low = 0; this.F_high = 1; this.F_medium = (F_low + F_high) / 2.0;
		this.CR_low = 0; this.CR_high = 1; this.CR_medium = (CR_low + CR_high) / 2.0;

		this.propability_F1 = 0.5; this.propability_F2 = 0.5;
		this.location_F1 = (F_low + F_medium) / 2.0; this.location_F2 = (F_medium + F_high) / 2.0;
		
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
		this.successCR1.clear(); this.successCR2.clear();
		selectIndividualsByF();
		selectIndividualsByCR();
	}

	public void generateControlParameters(Individual individual) {
		individual.setCrossoverRate(super.generateCrossoverRate(individual.isCR_flag() ? this.mean_CR1 : this.mean_CR2));
		individual.setDifferencialWeight(super.generateDifferencialWeight(individual.isF_flag() ? this.location_F1 : this.location_F2));
	}

	public void addSuccessful(Individual successful) {
		if (successful.isCR_flag())
			this.successCR1.add(successful.getCrossoverRate());
		else
			this.successCR2.add(successful.getCrossoverRate());
		if (successful.isF_flag())
			this.successF1.add(successful.getDifferencialWeight());
		else
			this.successF2.add(successful.getDifferencialWeight());
	}
	
	private void updateMeanCR(double mean_CR, List<Double> successControlParam) {
		BigDecimal newMeanCR = new BigDecimal(1);
		newMeanCR = newMeanCR.subtract(new BigDecimal(DEProperties.ADAPTATION_RATE)).multiply(new BigDecimal(mean_CR));
		BigDecimal meanA = new BigDecimal(successControlParam.isEmpty() ? 0.0 : Statistic.calculateMean(successControlParam));
		meanA = meanA.multiply(new BigDecimal(DEProperties.ADAPTATION_RATE));
		newMeanCR = newMeanCR.add(meanA);
		mean_CR = newMeanCR.doubleValue();
	}
	
}
