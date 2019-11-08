package br.ufrj.coc.cec2015.algorithm.ipop_jade;

import br.ufrj.coc.cec2015.algorithm.Population;
import br.ufrj.coc.cec2015.algorithm.de.DEProperties;
import br.ufrj.coc.cec2015.algorithm.jade.JADEHelper;
import br.ufrj.coc.cec2015.util.Properties;

public class IPOP_JADEHelper extends JADEHelper {
	private int countIncreases;
	
	public IPOP_JADEHelper() {
		super();
		this.initialize();
	}

	protected void initialize() {
		super.initialize();
		this.countIncreases = 0;
	}
	
	protected void increasePopulation(Population population, double determinant) {
		boolean useIncreasePopulation = DEProperties.MAX_INCREASE_POPULATION_WITH_EIG > 0;

		// variação nula do determinante da matriz de covariância significa que não houve melhora entre duas gerações
		double delta = Math.abs(determinant - population.getDeterminant());
		
		// variação muito pequena (1.0E-160) do determinante da matriz de covariância implica em dizer que toda a população convergiu para um mesmo ótimo
		boolean limitDetG = delta > 0 && delta < DEProperties.LIMIT_VARIANCE_DET_COVMATRIX;
		population.setDeterminant(determinant);

		boolean canIncrease = this.countIncreases < DEProperties.MAX_INCREASE_POPULATION_WITH_EIG;
		
		if (delta > 0 && delta < population.getMinDeterminant())
			population.setMinDeterminant(delta);
		
		if (useIncreasePopulation && canIncrease && limitDetG) {
			// increase population by keeping better pBest individuals
			int newSize = (int) (population.size() * 2);
			this.increase(population, newSize, super.selectPBestIndex());
			System.err.println(String.format("Increase population to %d", population.size()));
			this.initializeGeneration(population);
		}
	}

	protected boolean isUseEig() {
		double limitFactorMaxFES = DEProperties.LIMIT_FACTOR_MAXFES_WITH_EIG * (this.countIncreases == 0 ? 1 : this.countIncreases);
		return super.isUseEig() && Properties.ARGUMENTS.get().getEvolutionPercentage() <= limitFactorMaxFES;
	}
	
	private void increase(Population population, int newSize, int pBestIndex) {
		super.initializeSortedPopulation();
		if (newSize > population.size()) {
			// initialize
			Population sortedPopulation = super.getSortedPopulation();
			for (int index = pBestIndex + 1; index < sortedPopulation.size(); index++)
				sortedPopulation.initializeIndividual(index);
			
			// increase
			int increaseSize = newSize - population.size();
			for (int index = 0; index < increaseSize; index++)
				population.addIndividual();

			Properties.ARGUMENTS.get().setPopulationSize(newSize);
			this.countIncreases++;
		}
	}
}