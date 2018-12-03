package br.ufrj.coc.cec2015.algorithm.de;

import br.ufrj.coc.cec2015.algorithm.Algorithm;
import br.ufrj.coc.cec2015.algorithm.Individual;
import br.ufrj.coc.cec2015.algorithm.Initializable;
import br.ufrj.coc.cec2015.algorithm.Population;
import br.ufrj.coc.cec2015.util.Helper;
import br.ufrj.coc.cec2015.util.Statistic;

public class DE extends Algorithm {

	@Override
	public String[] getVariants() {
		return DEProperties.VARIANTS;
	}
	
	@Override
	public void setCurrentVariant(String variant) {
		DEProperties.setVariant(variant);
	}
	
	@Override
	public String getVariant() {
		return DEProperties.VARIANT.replace('/', '.');
	}
	
	@Override
	public Initializable getIntializable() {
		return new Initializable() {
			@Override
			public Individual newInitialized() {
				Individual individual = Helper.newIndividualInitialized();
				individual.setCrossoverRate(DEProperties.CROSSOVER_RATE);
				individual.setDifferencialWeight(DEProperties.DIFFERENTIAL_WEIGHT);
				return individual;
			}
		};
	}
	
	@Override
	public void initializeRun(int round) {
		super.initializeRun(round);
		DEHelper.initialize();
	}
	
	@Override
	public void run(Population population, Statistic statistic, int round) throws Exception {
		DEHelper.initializeGeneration(population);
		
		for (int index = 0; index < population.size(); index++) {
			Individual current = population.get(index);
			DEHelper.generateControlParameters(current);

			double[] trialVector = DEHelper.generateTrialVector(population, current);
			double functionValue = Helper.evaluate(trialVector);

			if (functionValue < current.getFunctionValue()) {				
				Individual inferior = current.clone();
				DEHelper.addInferior(inferior); // Xi,g => A

				current.setId(trialVector);
				current.setFunctionValue(functionValue);
				population.updateBestError(current);

				DEHelper.addSuccessful(current); // CRi => Scr | Fi => Sf
			}
			statistic.verifyEvaluationInstant(round, population);
		}

		DEHelper.finalizeGeneration();
	}
}
