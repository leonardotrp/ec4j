package br.ufrj.coc.cec2015.algorithm.ipop_jade;

import br.ufrj.coc.cec2015.algorithm.jade.JADEHelper;

public class IPOP_JADEHelper extends JADEHelper {
	/*
	private int countUnchanged;
	private double errorDifference, maxDistance;
	
	public IPOP_JADEHelper() {
		super();
		this.initialize();
	}

	protected void initialize() {
		super.initialize();
		this.countUnchanged = 0;
		this.errorDifference = Double.MAX_VALUE;
		this.maxDistance = -Double.MAX_VALUE;
	}
	protected void increasePopulation(Population population, double determinant) {
		
		Population sortedPopulation = super.getSortedPopulation();
		double worstFunctionValue = sortedPopulation.get(sortedPopulation.size() - 1).getFunctionValue();
		double bestFunctionValue = sortedPopulation.get(0).getFunctionValue();
		double errorDifference = worstFunctionValue - bestFunctionValue;

		double maxDistance = this.maxDistance();
		//boolean limitErrorReached = (errorDifference < Properties.MIN_ERROR_VALUE && maxDistance > 1);
		//if (limitErrorReached)
		//	System.err.println(String.format("F(%d): Vai reiniciar pela mínima variação do erro = %e e máxima distância = %e", Properties.ARGUMENTS.get().getFunctionNumber(), errorDifference, maxDistance));

		//boolean limitEqualitiesReached = false;
		//if (errorDifference == this.errorDifference && maxDistance == this.maxDistance) {
		//	limitEqualitiesReached = (this.countUnchanged++ == DEProperties.IPOP_MAX_ATTEMPTS_WITHOUT_POPULATION_CHANGE);
		//	System.err.println(String.format("Variação de erro e máxima distância não se alteraram %d", this.countUnchanged));
		//	if (limitEqualitiesReached)
		//		System.err.println(String.format("F(%d): Vai reiniciar pela mínima variação do erro = %e e máxima distância = %e", Properties.ARGUMENTS.get().getFunctionNumber(), errorDifference, maxDistance));
		//}
		//else
		//	this.countUnchanged = 0;
		
		if (limitErrorReached || limitEqualitiesReached) {
			// increase population by keeping better pBest individuals
			this.initialize_and_increase(population);
			this.initializeGeneration(population);
		}

		this.errorDifference = errorDifference;
		this.maxDistance = maxDistance;
	}

	private void initialize_and_increase(Population population) {
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
		this.errorDifference = Double.MAX_VALUE;
		this.maxDistance = -Double.MAX_VALUE;
	}
	*/
}