package br.ufrj.coc.cec2015.algorithm;

import br.ufrj.coc.cec2015.util.Helper;
import br.ufrj.coc.cec2015.util.Properties;
import br.ufrj.coc.cec2015.util.Statistic;

public abstract class Algorithm {
	
	public abstract void run(Population population, Statistic statistic, int round) throws Exception;
	
	public Initializable getIntializable() {
		return new Initializable() {
			@Override
			public Individual newInitialized() {
				return Helper.newIndividualInitialized();
			}
		};
	}
	
	public abstract String getVariant();
	
	public abstract String[] getVariants();
	
	public abstract void setCurrentVariant(String variant);

	public void initializeRun(int round) {
		Helper.COUNT_EVALUATIONS = 0;
	}
	
	public Population initializePopulation(Initializable initializable) {
		return new Population(initializable);
	}
	
	public boolean terminated(Population population) {
		return Helper.terminateRun(population);
	}

	/**
	 * @throws Exception
	 */
	public void main() throws Exception {
		
		for (int functionNumber : Properties.FUNCTIONS) { // loop functions

			Helper.changeFunction(Properties.INDIVIDUAL_SIZE, functionNumber);
			
			Statistic statistic = new Statistic(this);
			
			Initializable initializable = this.getIntializable();
			
			for (int round = 0; round < Properties.MAX_RUNS; round++) { // loop rounds or generations
	
				initializeRun(round);

				Population population = this.initializePopulation(initializable);

				while (!terminated(population)) {
					
					this.run(population, statistic, round);
					
				}
				statistic.addRound(population);
			}
			
			statistic.finalize();
		}
	}
}
