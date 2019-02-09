package br.ufrj.coc.cec2015.algorithm.de;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.ufrj.coc.cec2015.algorithm.Algorithm;
import br.ufrj.coc.cec2015.algorithm.Individual;
import br.ufrj.coc.cec2015.algorithm.Initializable;
import br.ufrj.coc.cec2015.algorithm.Population;
import br.ufrj.coc.cec2015.util.Helper;
import br.ufrj.coc.cec2015.util.Properties;
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
	
	private List<Double> historyOfBests = new ArrayList<>();
	private int maxSizeHistory;
	private void initializeHistoryOfBests(int populationSize) {
		this.historyOfBests.clear();
		this.maxSizeHistory = 10 + ((70 * Properties.INDIVIDUAL_SIZE) / populationSize);
	}
	private void addBestInHistory(Double bestValue) {
		if (this.isHistoryOfBestFull()) {
			this.historyOfBests.add(0, bestValue);
			int lastIndex = this.historyOfBests.size() - 1;
			this.historyOfBests.remove(lastIndex);
		}
		else
			this.historyOfBests.add(0, bestValue);
	}
	private boolean isHistoryOfBestFull() {
		return this.historyOfBests.size() == this.maxSizeHistory;
	}

	private boolean terminatedToRestart(Population population) throws Exception {
		if (terminated(population))
			return true;

		// sort population by fitness
		Population sortedPopulation = (Population) population.clone();
		int size = sortedPopulation.size();
		Collections.sort(sortedPopulation.getIndividuals());

		double minHistory = this.historyOfBests.isEmpty() ? 0 : (Double) Collections.min(this.historyOfBests);
		double maxHistory = this.historyOfBests.isEmpty() ? 0 : (Double) Collections.max(this.historyOfBests);

		// TOLFUN - Critério pela tolerância mínima entre o melhor e pior resultado
		double minValue = Math.min(minHistory, Helper.getError(sortedPopulation.get(0)));
		double maxValue = Math.max(maxHistory, Helper.getError(sortedPopulation.get(size - 1)));
		double difference = maxValue - minValue;
		if (difference < DEProperties.STOP_TOL_FUN) {
			System.err.println("TolFun: function value changes ("+difference+") below stopTolFun = " + DEProperties.STOP_TOL_FUN);
			return true;
		}
		// TOLFUNHIST - Critério pela tolerância mínima entre o melhor e pior da geração
		if (maxHistory > minHistory && maxHistory - minHistory < DEProperties.STOP_TOL_FUN_HIST) {
			System.err.println("TolFunHist: history of function value changes below stopTolFunHist = " + DEProperties.STOP_TOL_FUN_HIST);
			return true;
		}
		// EQUALFUNVALUES - Pára se todos os valores em T forem iguais
		if (this.isHistoryOfBestFull() && maxHistory == minHistory) {
			System.err.println("EqualFunValues: the range of the best objective function values of the history is zero");
			return true;
		}
		/*
		// TOLX
		double tolx = Math.max(options.stopTolX, options.stopTolXFactor * minstartsigma);
		if (sigma * maxsqrtdiagC < tolx && sigma * math.max(math.abs(pc)) < tolx) {
		    System.err.println("TolX or TolXFactor: standard deviation below " + tolx);
		    return true;
		}
		// TOLXUP
		if (sigma * maxsqrtdiagC > options.stopTolUpXFactor * maxstartsigma) {
			System.err.println("TolUpX: standard deviation increased by more than stopTolUpXFactor=" + options.stopTolUpXFactor + ", larger initial standard deviation recommended");
			return true;
		}
		*/
		return false;
	}

	@Override
	protected void executeRoud(Initializable initializable, Statistic statistic, int round) throws Exception {
		if (DEProperties.INCREASE_POPULATION && DEProperties.NUMBER_OF_RESTARTS > 0) {
			initializeRun(round);

			int lambda = Properties.POPULATION_SIZE;
			Population population = new Population(initializable, lambda);
			boolean canRestart = true;
			for (int indexRestart = 0; canRestart && indexRestart <= DEProperties.NUMBER_OF_RESTARTS; indexRestart++) {
				DEHelper.initialize();
				if (indexRestart > 0) {
					int newPopulationSize = (int) Math.ceil(lambda * Math.pow(DEProperties.INCREASE_POPSIZE_FACTOR, indexRestart));
					System.err.println("RESTART ("+indexRestart+")! Increase population size from " + lambda + " to " + newPopulationSize);
					lambda = newPopulationSize;
					population.increase(lambda);
				}
				this.initializeHistoryOfBests(lambda);

				while (!terminatedToRestart(population)) {
					this.run(population, statistic, round);
					this.addBestInHistory(population.getBestError());
				}

				canRestart = !terminated(population);
				if (!canRestart && !this.historyOfBests.isEmpty() && this.historyOfBests.get(0) > 1.0E+2) {
					System.err.println("tolErroMax: the best objective function values is above tolErroMax="+Helper.getGlobalOptimum());
					canRestart = true;
				}
			}
			statistic.addRound(population);
		} else {
			super.executeRoud(initializable, statistic, round);
		}
	}
}
