package br.ufrj.coc.cec2015.algorithm.ipop_jade;

import br.ufrj.coc.cec2015.algorithm.Population;
import br.ufrj.coc.cec2015.algorithm.de.DEProperties;
import br.ufrj.coc.cec2015.algorithm.jade.JADEHelper;
import br.ufrj.coc.cec2015.util.Properties;

public class IPOP_JADEHelper extends JADEHelper {
	private int countUnchanged;
	private double bestError;
	private boolean useEig;
	
	public IPOP_JADEHelper() {
		super();
		this.initialize();
	}

	protected void initialize() {
		super.initialize();
		this.countUnchanged = 0;
		this.bestError = Double.MAX_VALUE;
		this.useEig = super.isUseEig();
	}
	
	protected void increasePopulation(Population population, double determinant) {
		double rangeDetMatConv = Math.abs(determinant - population.getDetMatConv());
		population.setDetMatConv(determinant);

		// variação nula do determinante da matriz de covariância significa que não houve melhora entre duas gerações
		if (rangeDetMatConv == 0 && this.bestError == population.getBestError() && (this.bestError / Properties.MIN_ERROR_VALUE) > 100) {
			// variação nula do determinante da matriz de covariância significa que não houve variação dos autovetores entre as gerações
			if (this.countUnchanged++ == DEProperties.IPOP_MAX_ATTEMPTS_WITHOUT_POPULATION_CHANGE) {
				// increase population by keeping better pBest individuals
				this.initialize_and_increase(population);
				this.initializeGeneration(population);
			}
			System.err.println(String.format("Não melhorou o menor erro (%e) %d vezes...", this.bestError, this.countUnchanged));
		}
		else if (this.bestError != population.getBestError())
			this.countUnchanged = 0;

		this.bestError = super.getPopulation().getBestError();
	}

	protected boolean isUseEig() {
		return this.useEig;
	}
	
	private void initialize_and_increase(Population population) {
		super.initializeSortedPopulation();
		
		// initialize
		Population sortedPopulation = super.getSortedPopulation();
		int pBestIndex = super.selectPBestIndex(DEProperties.IPOP_GREEDINESS);
		for (int index = pBestIndex + 1; index < sortedPopulation.size(); index++)
			sortedPopulation.initializeIndividual(index);
			
		int increaseSize = (int) (population.size() * (DEProperties.IPOP_FACTOR_NEW_POPSIZE / 100));
		if (increaseSize > 0) {
			// increase
			for (int index = 0; index < increaseSize; index++)
				population.addIndividual();

			Properties.ARGUMENTS.get().setPopulationSize(population.size());
		}
		this.countUnchanged = 0;
		this.bestError = Double.MAX_VALUE;
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