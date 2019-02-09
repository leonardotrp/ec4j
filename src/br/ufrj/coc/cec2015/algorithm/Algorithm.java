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
	
	protected boolean terminated(Population population) {
		return Helper.terminateRun(population);
	}
	
	protected void executeRoud(Initializable initializable, Statistic statistic, int round) throws Exception {
		initializeRun(round);
		Population population = new Population(initializable);
		while (!terminated(population)) {
			this.run(population, statistic, round);
		}
		statistic.addRound(population);
	}

	/**
	 * @throws Exception
	 */
	public void main() throws Exception {
		Statistic statistic = new Statistic(this);
		for (int functionNumber : Properties.FUNCTIONS) { // loop functions
			Helper.changeFunction(Properties.INDIVIDUAL_SIZE, functionNumber);
			statistic.startFunction();
			Initializable initializable = this.getIntializable();
			for (int round = 0; round < Properties.MAX_RUNS; round++) { // loop rounds or generations
				executeRoud(initializable, statistic, round);
			}
			statistic.endFunction();
		}
		statistic.end();
	}
}