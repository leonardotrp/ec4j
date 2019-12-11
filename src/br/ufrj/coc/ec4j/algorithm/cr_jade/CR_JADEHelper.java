package br.ufrj.coc.ec4j.algorithm.cr_jade;

import br.ufrj.coc.ec4j.algorithm.Population;
import br.ufrj.coc.ec4j.algorithm.de.DEProperties;
import br.ufrj.coc.ec4j.algorithm.jade.JADEHelper;
import br.ufrj.coc.ec4j.util.Helper;
import br.ufrj.coc.ec4j.util.Properties;

public class CR_JADEHelper extends JADEHelper {
	private int evalPerc;

	protected void initialize() {
		super.initialize();
		this.evalPerc = 0;
	}

	public void initializeGeneration(Population population) {
		super.initializeGeneration(population);
		
		double funcValDifference = Helper.getFunctionValueDifference(population);
		double maxDistance = Helper.getMaxDistance(population);

		if (DEProperties.CR_MAXFES_INTERVAL > 0) {
			int currentEvalPerc = (int) (Properties.ARGUMENTS.get().getEvolutionPercentage() * 100);
			int interv = (int) (DEProperties.CR_MAXFES_INTERVAL * 100);
			if (currentEvalPerc > this.evalPerc && (currentEvalPerc % interv) == 0) {
				this.evalPerc = currentEvalPerc;
				double funcValueDiffInterval = Math.abs(population.getFuncValDiff() - funcValDifference);
				double maxDistInterval = Math.abs(population.getMaxDistance() - maxDistance);
				//System.err.println(String.format("Test stagnation %.2f: funcValueDiffInterval = %e / maxDistInterval = %e", Properties.ARGUMENTS.get().getEvolutionPercentage(), funcValueDiffInterval, maxDistInterval));
				boolean criteria1 = funcValueDiffInterval == 0.0 && maxDistInterval == 0.0;
				boolean criteria2 = funcValueDiffInterval == 0.0 && maxDistInterval < DEProperties.CR_MAX_DIST;
				boolean criteria3 = funcValueDiffInterval < DEProperties.CR_MAX_FUNCVAL && maxDistInterval == 0.0;
				boolean stagnation = criteria1 || criteria2 || criteria3;
				if (stagnation)
					this.controlledRestart(population);
				population.setFuncValDiff(funcValDifference);
				population.setMaxDistance(maxDistance);
			}
		}
	}
	
	/**
	 * restart population by keeping better individual
	 * @param population
	 */
	private void controlledRestart(Population population) {
		System.err.println(String.format("RESTART (%d)!", population.getCountRestart()));
		// initialize
		Population sortedPopulation = super.getSortedPopulation();
		for (int index = 1; index < sortedPopulation.size(); index++)
			sortedPopulation.initializeIndividual(index);
		/*
		int increaseSize = (int) (population.size() * (DEProperties.CR_FACTOR_NEW_POPSIZE / 100));
		if (increaseSize > 0) {
			// increase
			for (int index = 0; index < increaseSize; index++)
				population.addIndividual();
			Properties.ARGUMENTS.get().setPopulationSize(population.size());
		}
		*/

		population.setFuncValDiff(0);
		population.setMaxDistance(0);

		population.incCountRestart();
		this.initializeGeneration(population);
	}
}