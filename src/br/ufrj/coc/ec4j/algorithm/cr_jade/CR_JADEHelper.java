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
		/*
		if (DEProperties.CR_MAXFES_INTERVAL > 0) {
			int currentEvalPerc = (int) (Properties.ARGUMENTS.get().getEvolutionPercentage() * 100);
			int interv = (int) (DEProperties.CR_MAXFES_INTERVAL * 100);
			if (currentEvalPerc > this.evalPerc && (currentEvalPerc % interv) == 0) {
				
				this.evalPerc = currentEvalPerc;

				// CRITÉRIO PELA DIFERENÇA IMEDIATA ENTRE POPULAÇÕES
				// --------------------------------------------------------------
				double funcValDifference = Helper.getFunctionValueDifference(population);
				double maxDistance = Helper.getMaxDistance(population);

				double funcValueDiffInterval = Math.abs(population.getFuncValDiff() - funcValDifference); // houve mínima variabilidade entre o melhor e o pior indivíduo da população
				double maxDistInterval = Math.abs(population.getMaxDistance() - maxDistance); // houve mínima dispersão dos individuos no espaço de busca
				System.err.println(String.format("Test stagnation %.2f: funcValueDiffInterval = %e / maxDistInterval = %e", Properties.ARGUMENTS.get().getEvolutionPercentage(), funcValueDiffInterval, maxDistInterval));

				population.setFuncValDiff(funcValDifference);
				population.setMaxDistance(maxDistance);

				//boolean criteria1 = funcValueDiffInterval == 0.0 && maxDistInterval == 0.0;
				//boolean criteria2 = funcValueDiffInterval == 0.0 && maxDistInterval < DEProperties.CR_MAX_DIST;
				//boolean criteria3 = funcValueDiffInterval < DEProperties.CR_MAX_FUNCVAL && maxDistInterval == 0.0;
				//boolean stagnation = criteria1 || criteria2 || criteria3;

				boolean stagnation = funcValueDiffInterval < DEProperties.CR_MAX_FUNCVAL && maxDistInterval < DEProperties.CR_MAX_DIST;
				if (stagnation)
					this.controlledRestart(population);
				
				// CRITÉRIO PELA MÉDIA DAS ÚLTIMAS N GERAÇÕES
				// --------------------------------------------------------------
				//
				//population.getFuncValDiffs().add(funcValueDiffInterval);
				//population.getMaxDistances().add(maxDistInterval);
				//if (population.getFuncValDiffs().size() > 1) { // para calcular a média, precisa ter pelo menos dois valores
				//	double medianFuncValDiffs = Helper.calculateMedian(population.getFuncValDiffs());
				//	double medianMaxDistances = Helper.calculateMedian(population.getMaxDistances());
				//	System.err.println(String.format("Test stagnation %.2f: queue size = %d / medianFuncValDiffs = %e / medianMaxDistances = %e", Properties.ARGUMENTS.get().getEvolutionPercentage(), population.getFuncValDiffs().size(), medianFuncValDiffs, medianMaxDistances));
				//	boolean stagnation = medianFuncValDiffs < DEProperties.CR_MAX_FUNCVAL && medianMaxDistances < DEProperties.CR_MAX_DIST;
				//	if (stagnation) {
				//		this.controlledRestart(population);
				//	}
				//}
			}
		}
		*/
		this.checkStagnationPolakova(population);
	}
	

	private void checkStagnationPolakova(Population population) {
		double funcValDifference = Helper.getFunctionValueDifference(population);
		double maxDistance = Helper.getMaxDistance(population);
		//System.err.println(String.format("Test POLAKOVA stagnation %.2f: funcValDifference = %e / maxDistance = %e", Properties.ARGUMENTS.get().getEvolutionPercentage(), funcValDifference, maxDistance));
		boolean stagnation = funcValDifference < DEProperties.CR_MAX_FUNCVAL && maxDistance < DEProperties.CR_MAX_DIST;
		if (stagnation) {
			this.controlledRestart(population);
		}
	}


	/**
	 * restart population by keeping better individual
	 * @param population
	 */
	private void controlledRestart(Population population) {
		System.err.println(String.format("RESTART POP (%d)!", population.getCountRestart()));

		population.randonRestart();
		population.setFuncValDiff(0);
		population.setMaxDistance(0);
		population.incCountRestart();

		this.initializeGeneration(population);
	}
}