package br.ufrj.coc.cec2015.algorithm.ipop_jade;

import br.ufrj.coc.cec2015.algorithm.Population;
import br.ufrj.coc.cec2015.algorithm.de.DEProperties;
import br.ufrj.coc.cec2015.algorithm.jade.JADEHelper;
import br.ufrj.coc.cec2015.util.Properties;

public class IPOP_JADEHelper extends JADEHelper {
	private int countIncreases;
	private int countUnchanged;
	private double bestError;
	
	public IPOP_JADEHelper() {
		super();
		this.initialize();
	}

	protected void initialize() {
		super.initialize();
		this.countIncreases = 0;
		this.countUnchanged = 0;
		this.bestError = Double.MAX_VALUE;
	}
	
	protected void increasePopulation(Population population, double determinant) {
		boolean canIncrease = this.countIncreases < DEProperties.IPOP_MAX_INCREASE_POPULATION;
		boolean limitDetG = false;
		boolean limitUnchanged = false;

		double rangeDetMatConv = Math.abs(determinant - population.getDetMatConv());
		population.setDetMatConv(determinant);

		// variação nula do determinante da matriz de covariância significa que não houve melhora entre duas gerações
		if (rangeDetMatConv == 0) {
			if (DEProperties.IPOP_MAX_ATTEMPTS_WITHOUT_POPULATION_CHANGE > 0) {
				if (this.bestError == population.getBestError()) {
					limitUnchanged = (this.countUnchanged++ == DEProperties.IPOP_MAX_ATTEMPTS_WITHOUT_POPULATION_CHANGE);
				}
				this.bestError = population.getBestError();
			}
		}
		else
			// variação muito pequena (1.0E-200) do determinante da matriz de covariância implica em dizer que toda a população convergiu para um mesmo ótimo
			limitDetG = rangeDetMatConv < Math.pow(10, -DEProperties.IPOP_LIMIT_RANGE_DET_COVMATRIX * (0.5 * this.countIncreases + 1));

		if (canIncrease && (limitDetG || limitUnchanged)) {
			// increase population by keeping better pBest individuals
			int newSize = (int) (population.size() * 2);
			this.increase(population, newSize, super.selectPBestIndex());
			System.err.println(String.format("Increase population to %d: reason(limitDetG=%s, limitUnchanged=%s)", population.size(), limitDetG, limitUnchanged));
			this.countUnchanged = 0;
			this.bestError = Double.MAX_VALUE;
			this.initializeGeneration(population);
		}
	}

	protected boolean isUseEig() {
		double limitFactorMaxFES = DEProperties.IPOP_LIMIT_FACTOR_MAXFES * (this.countIncreases == 0 ? 1 : this.countIncreases);
		return super.isUseEig() && Properties.ARGUMENTS.get().getEvolutionPercentage() <= limitFactorMaxFES;
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
	/*
	private double computeEuclidianDistances() {
		Individual best = super.getPopulation().getBest();
		double minDistanceEuclidian = Double.MAX_VALUE;
		double maxDistanceEuclidian = Double.MIN_VALUE;
		for (Individual individual : super.getPopulation().getIndividuals()) {
			if (!best.equals(individual)) {
				double euclidianDistance = new EuclideanDistance().compute(best.getId(), individual.getId());
				if (euclidianDistance > maxDistanceEuclidian)
					maxDistanceEuclidian = euclidianDistance;
				else if (euclidianDistance < minDistanceEuclidian)
					minDistanceEuclidian = euclidianDistance;
			}
		}
		return Math.abs(maxDistanceEuclidian - minDistanceEuclidian);
	}
	*/
}