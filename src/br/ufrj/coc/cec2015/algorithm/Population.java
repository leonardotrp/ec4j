package br.ufrj.coc.cec2015.algorithm;

import java.util.ArrayList;
import java.util.List;

import br.ufrj.coc.cec2015.util.Helper;
import br.ufrj.coc.cec2015.util.Properties;

public class Population implements Cloneable {
	private List<Individual> individuals;
	private Initializable initializable;
	private Individual best; // global best
	private boolean minErrorValueFound;
	
	public Population(Initializable initializable) {
		super();
		this.initializable = initializable;
		this.initialize();
	}
	
	public Population() {
		super();
	}
	
	public void load(double[][] population) {
		this.individuals = new ArrayList<Individual>(population.length);
		double bestError = Double.MAX_VALUE;
		for (int index = 0; index < population.length; index++) {
			Individual individual = new Individual(population[index]);
			this.individuals.add(individual);
			
			double error = Helper.getError(individual);
			if (index == 0 || error < bestError) {
				bestError = error;
				this.best = individual;
			}
		}
	}
	
	public void initialize() {
		this.individuals = new ArrayList<Individual>(Properties.POPULATION_SIZE);
		double bestError = Double.MAX_VALUE;
		for (int index = 0; index < Properties.POPULATION_SIZE; index++) {
			Individual individual = this.initializable.newInitialized();
			this.individuals.add(individual);
			
			double error = Helper.getError(individual);
			if (index == 0 || error < bestError) {
				bestError = error;
				this.best = individual;
			}
		}
	}
	
	public void initialize(int index) {
		Individual individual = this.initializable.newInitialized();
		this.individuals.set(index, individual);
	}
	
	public List<Individual> getIndividuals() {
		return individuals;
	}

	public Individual getBest() {
		return best;
	}

	public double getBestError() {
		return Helper.getError(this.best);
	}

	public boolean isMinErrorValueFound() {
		return minErrorValueFound;
	}

	public void setMinErrorValueFound(boolean minErrorValueFound) {
		this.minErrorValueFound = minErrorValueFound;
	}

	public int size() {
		return this.individuals == null ? 0 : this.individuals.size();
	}
	
	public Individual remove(int index) {
		return this.individuals.remove(index);
	}
	
	public Individual get(int index) {
		return this.individuals.get(index);
	}
	
	public int indexOf(Individual individual) {
		return this.individuals.indexOf(individual);
	}
	
	public void updateBestError(Individual individual) {
		double error = Helper.getError(individual);
		double bestError = Helper.getError(this.best);
		if (error < bestError) {
			this.best = (Individual) individual.clone();
		}
	}
	
	@Override
	public Population clone() throws CloneNotSupportedException {
		Population clone = (Population) super.clone();
		clone.individuals = new ArrayList<Individual>(Properties.POPULATION_SIZE);
		clone.individuals.addAll(this.individuals);
		return clone;
	}
	
	public Object cloneAll() throws CloneNotSupportedException {
		Population clone = (Population) super.clone();
		clone.individuals = new ArrayList<Individual>(Properties.POPULATION_SIZE);
		for (Individual individual : this.individuals) {
			Individual individualClone = (Individual) individual.clone();
			clone.individuals.add(individualClone);
		}
		return clone;
	}
}