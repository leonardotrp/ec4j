package br.ufrj.coc.ec4j.algorithm.de;

import br.ufrj.coc.ec4j.algorithm.Algorithm;
import br.ufrj.coc.ec4j.algorithm.AlgorithmHelper;
import br.ufrj.coc.ec4j.algorithm.Individual;
import br.ufrj.coc.ec4j.algorithm.Initializable;
import br.ufrj.coc.ec4j.algorithm.Population;
import br.ufrj.coc.ec4j.util.Helper;
import br.ufrj.coc.ec4j.util.Properties;
import br.ufrj.coc.ec4j.util.Statistic;

public class DE extends Algorithm {
	
	@Override
	protected AlgorithmHelper newInstanceHelper() {
		return new DEHelper();
	}

	@Override
	public String[] getVariants() {
		return DEProperties.VARIANTS;
	}

	@Override
	public String getInfo() {
		return DEProperties.INFO;
	}

	@Override
	public Initializable getIntializable() {
		return new Initializable() {
			private void setParameters(Individual individual) {
				individual.setCrossoverRate(DEProperties.CROSSOVER_RATE);
				individual.setDifferencialWeight(DEProperties.DIFFERENTIAL_WEIGHT);
			}
			@Override
			public Individual newInitialized() {
				Individual individual = Helper.newIndividualInitialized();
				setParameters(individual);
				return individual;
			}
			@Override
			public Individual newInitialized(double[] id) {
				Individual individual = Helper.newIndividualInitialized(id);
				setParameters(individual);
				return individual;
			}
		};
	}
	
	public DEHelper getDEHelper() {
		return (DEHelper) Properties.HELPER.get();
	}
	
	@Override
	public void run(Population population, Statistic statistic, int round) throws Exception {
		getDEHelper().initializeGeneration(population);
		for (Individual current : population.getIndividuals()) {
			getDEHelper().generateControlParameters(current);

			double[] trialVector = getDEHelper().generateTrialVector(current);
			double functionValue = Properties.ARGUMENTS.get().evaluateFunction(trialVector);

			if (functionValue < current.getFunctionValue()) {
				Individual inferior = current.clone();
				getDEHelper().addInferior(inferior); // Xi,g => A

				current.setId(trialVector);
				current.setFunctionValue(functionValue);
				population.updateBestError(current);	

				getDEHelper().addSuccessful(current); // CRi => Scr | Fi => Sf
			}
			statistic.verifyEvaluationInstant(round, population);
		}
		getDEHelper().finalizeGeneration();
	}
}
