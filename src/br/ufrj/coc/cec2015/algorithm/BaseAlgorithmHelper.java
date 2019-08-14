package br.ufrj.coc.cec2015.algorithm;

import br.ufrj.coc.cec2015.util.Properties;

public abstract class BaseAlgorithmHelper implements AlgorithmHelper {
	private Population population;
	
	@Override
	public void initializeGeneration(Population population) {
		this.population = population;
		Properties.ARGUMENTS.get().incrementCountGenerations();
	}

	public Population getPopulation() {
		return this.population;
	}
}
