package br.ufrj.coc.cec2015.algorithm.abc;

import br.ufrj.coc.cec2015.algorithm.Algorithm;
import br.ufrj.coc.cec2015.algorithm.Individual;
import br.ufrj.coc.cec2015.algorithm.Initializable;
import br.ufrj.coc.cec2015.algorithm.Population;
import br.ufrj.coc.cec2015.algorithm.de.DEProperties;
import br.ufrj.coc.cec2015.util.Statistic;

public class ABC extends Algorithm {

	@Override
	public String[] getVariants() {
		return ABCProperties.VARIANTS;
	}
	
	@Override
	public void setCurrentVariant(String variant) {
		ABCProperties.setVariant(variant);
	}
	
	@Override
	public String getVariant() {
		return ABCProperties.VARIANT;
	}
	
	@Override
	public Initializable getIntializable() {
		return new Initializable() {
			@Override
			public Individual newInitialized() {
				Individual individual = ABCHelper.newInstanceFood();
				individual.setCrossoverRate(DEProperties.CROSSOVER_RATE);
				individual.setDifferencialWeight(DEProperties.DIFFERENTIAL_WEIGHT);
				return individual;
			}
		};
	}
	
	@Override
	public void run(Population foodSources, Statistic statistic, int round) throws Exception {

		// send employee bees
		ABCHelper.sendEmployedBees(foodSources, statistic, round);
		
		// send onlooker bees
		ABCHelper.sendOnLookerBees(foodSources, statistic, round);
		
		// send sout bees
		ABCHelper.sendScoutBees(foodSources, statistic, round);
	}
}
