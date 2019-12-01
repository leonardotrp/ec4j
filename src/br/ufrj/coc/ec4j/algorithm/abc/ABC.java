package br.ufrj.coc.ec4j.algorithm.abc;

import br.ufrj.coc.ec4j.algorithm.Algorithm;
import br.ufrj.coc.ec4j.algorithm.AlgorithmHelper;
import br.ufrj.coc.ec4j.algorithm.Individual;
import br.ufrj.coc.ec4j.algorithm.Initializable;
import br.ufrj.coc.ec4j.algorithm.Population;
import br.ufrj.coc.ec4j.algorithm.de.DEProperties;
import br.ufrj.coc.ec4j.util.Properties;
import br.ufrj.coc.ec4j.util.Statistic;

public class ABC extends Algorithm {

	@Override
	public String[] getVariants() {
		return ABCProperties.VARIANTS;
	}
	
	@Override
	public String getInfo() {
		return null;
	}
	
	@Override
	protected AlgorithmHelper newInstanceHelper() {
		return new ABCHelper();
	}

	public ABCHelper getABCHelper() {
		return (ABCHelper) Properties.HELPER.get();
	}
	
	@Override
	public Initializable getIntializable() {
		return new Initializable() {
			@Override
			public Individual newInitialized() {
				Individual individual = ABCHelper.newInstanceFood();
				individual.setCrossoverRate(DEProperties.CROSSOVER_RATE);
				return individual;
			}

			@Override
			public Individual newInitialized(double[] id) {
				return null;
			}
		};
	}
	
	@Override
	public void run(Population foodSources, Statistic statistic, int round) throws Exception {
		getABCHelper().initializeGeneration(foodSources);

		// send employee bees
		getABCHelper().sendEmployedBees(statistic, round);
		
		// send onlooker bees
		getABCHelper().sendOnLookerBees(statistic, round);
		
		// send sout bees
		getABCHelper().sendScoutBees(statistic, round);
	}
}
