package br.ufrj.coc.cec2015.algorithm.abc;

import br.ufrj.coc.cec2015.algorithm.Algorithm;
import br.ufrj.coc.cec2015.algorithm.AlgorithmHelper;
import br.ufrj.coc.cec2015.algorithm.Individual;
import br.ufrj.coc.cec2015.algorithm.Initializable;
import br.ufrj.coc.cec2015.algorithm.Population;
import br.ufrj.coc.cec2015.algorithm.de.DEProperties;
import br.ufrj.coc.cec2015.util.Properties;
import br.ufrj.coc.cec2015.util.Statistic;

public class ABC extends Algorithm {

	@Override
	public String[] getVariants() {
		return ABCProperties.VARIANTS;
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
