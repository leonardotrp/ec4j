package br.ufrj.coc.cec2015.algorithm;

import java.io.File;

import br.ufrj.coc.cec2015.util.FileUtil;
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
			@Override
			public Individual newInitialized(double[] id) {
				return Helper.newIndividualInitialized(id);
			}
		};
	}

	public abstract String[] getVariants();
	
	private void initializeRound(int round) {
		Properties.HELPER.set(newInstanceHelper());
		Properties.ARGUMENTS.get().initializeCountEvaluations();
	}
	
	protected abstract AlgorithmHelper newInstanceHelper();
	
	protected boolean terminated(Population population) {
		return Helper.terminateRun(population);
	}
	
	protected void executeRoud(Initializable initializable, Statistic statistic, int round) throws Exception {
		statistic.startRound();
		initializeRound(round);
		File initialPopulationFile = FileUtil.getInitialPopulationFile();
		Population population = initialPopulationFile != null && round == 0 ? new Population(initializable, initialPopulationFile) : new Population(initializable);
		while (!terminated(population)) {
			this.run(population, statistic, round);
		}
		statistic.addRound(population);
	}

	public void main(String name, int individualSize, String variant) throws Exception {
		for (int functionNumber : Properties.FUNCTION_NUMBERS) { // loop functions
			Runnable runnable = () -> {
				try {
					AlgorithmArguments arguments = new AlgorithmArguments(name, variant, functionNumber, individualSize);
					Properties.ARGUMENTS.set(arguments);

					Statistic statistic = new Statistic();
					Initializable initializable = this.getIntializable();
					for (int round = 0; round < Properties.MAX_RUNS; round++) { // loop rounds or generations
						executeRoud(initializable, statistic, round);
					}
					statistic.close();

				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			};
			Thread thread = new Thread(runnable);
			thread.start();
		}
	}
}