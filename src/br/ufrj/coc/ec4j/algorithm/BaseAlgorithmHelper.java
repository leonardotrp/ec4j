package br.ufrj.coc.ec4j.algorithm;

import br.ufrj.coc.ec4j.util.Properties;

public abstract class BaseAlgorithmHelper implements AlgorithmHelper {
	private Population population;
	
	@Override
	public void initializeGeneration(Population population) {
		this.population = population;
		Properties.ARGUMENTS.get().incrementCountGenerations();
	}

	@Override
	public Population getPopulation() {
		return this.population;
	}
}
