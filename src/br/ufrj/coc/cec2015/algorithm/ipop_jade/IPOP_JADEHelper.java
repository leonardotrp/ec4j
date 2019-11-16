package br.ufrj.coc.cec2015.algorithm.ipop_jade;

import br.ufrj.coc.cec2015.algorithm.Population;
import br.ufrj.coc.cec2015.algorithm.de.DEProperties;
import br.ufrj.coc.cec2015.algorithm.jade.JADEHelper;
import br.ufrj.coc.cec2015.util.Properties;

public class IPOP_JADEHelper extends JADEHelper {
	private int countIPOP;
	private int countUnchanged;
	private double bestError;
	
	public IPOP_JADEHelper() {
		super();
		this.initialize();
	}

	protected void initialize() {
		super.initialize();
		this.countIPOP = 0;
		this.countUnchanged = 0;
		this.bestError = Double.MAX_VALUE;
	}
	
	protected void increasePopulation(Population population, double determinant) {
		boolean canIncrease = this.countIPOP < DEProperties.IPOP_MAX_INITIALIZE_AND_INCREASE;
		boolean limitDetG = false;
		boolean limitUnchanged = false;

		double rangeDetMatConv = Math.abs(determinant - population.getDetMatConv());
		population.setDetMatConv(determinant);

		// variação nula do determinante da matriz de covariância significa que não houve melhora entre duas gerações
		if (rangeDetMatConv == 0) {
			if (DEProperties.IPOP_MAX_ATTEMPTS_WITHOUT_POPULATION_CHANGE > 0) {
				if (this.bestError == population.getBestError() && this.bestError > 0.001)
					limitUnchanged = (this.countUnchanged++ == DEProperties.IPOP_MAX_ATTEMPTS_WITHOUT_POPULATION_CHANGE);
				this.bestError = population.getBestError();
			}
		}
		else
			// variação muito pequena (1.0E-200) do determinante da matriz de covariância implica em dizer que toda a população convergiu para um mesmo ótimo
			limitDetG = rangeDetMatConv < Math.pow(10, -DEProperties.IPOP_LIMIT_RANGE_DET_COVMATRIX * (this.countIPOP + 1));

		if (canIncrease && (limitDetG || limitUnchanged)) {
			if (limitUnchanged)
				System.err.println(String.format("%d vezes sem alterar o menor erro %e", this.countUnchanged, this.bestError));
			if (limitDetG)
				System.err.println(String.format("rangeDetMatConv = %e", rangeDetMatConv));
			
			// increase population by keeping better pBest individuals
			this.initialize_and_increase(population);
			System.err.println(String.format("Initialize and increase population: reason(limitDetG=%s, limitUnchanged=%s)", limitDetG, limitUnchanged));
			this.countUnchanged = 0;
			this.bestError = Double.MAX_VALUE;
			this.initializeGeneration(population);
		}
	}

	protected boolean isUseEig() {
		double limitFactorMaxFES = (1.0 / DEProperties.IPOP_MAX_INITIALIZE_AND_INCREASE) * (this.countIPOP + 1);
		return super.isUseEig() && Properties.ARGUMENTS.get().getEvolutionPercentage() <= limitFactorMaxFES;
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
		this.countIPOP++;
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