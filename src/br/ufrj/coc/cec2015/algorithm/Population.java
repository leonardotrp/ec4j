package br.ufrj.coc.cec2015.algorithm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.math3.linear.RealMatrix;

import br.ufrj.coc.cec2015.util.Helper;
import br.ufrj.coc.cec2015.util.Properties;

public class Population implements Cloneable {
	private List<Individual> individuals;
	private Initializable initializable;
	private Individual best; // global best
	private boolean minErrorValueFound;
	private RealMatrix eigenvectors;
	
	public Population(Initializable initializable, int populationSize) {
		super();
		this.initializable = initializable;
		this.initialize(populationSize);
	}

	public Population(Initializable initializable) {
		this(initializable, Properties.ARGUMENTS.get().getPopulationSize());
	}
	
	public Population(Initializable initializable, File csvPopulationFile) {
		super();
		this.initializable = initializable;
		this.load(csvPopulationFile);
	}
	
	public void increase(int newSize) {
		if (newSize > this.size()) {
			int increaseSize = newSize - this.size();
			for (int index = 0; index < increaseSize; index++) {
				Individual individual = this.initializable.newInitialized();
				this.individuals.add(individual);
				
				double error = Helper.getError(individual);
				if (error < this.getBestError()) {
					this.best = individual;
				}
			}
		}
	}
	
	private void initialize(int populationSize) {
		this.individuals = new ArrayList<Individual>(populationSize);
		double bestError = Double.MAX_VALUE;
		for (int index = 0; index < populationSize; index++) {
			Individual individual = this.initializable.newInitialized();
			this.individuals.add(individual);
			
			double error = Helper.getError(individual);
			if (index == 0 || error < bestError) {
				bestError = error;
				this.best = individual;
			}
		}
	}
	
	public void initializeIndividual(int index) {
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

	public RealMatrix getEigenvectors() {
		return eigenvectors;
	}

	public void setEigenvectors(RealMatrix eigenvectors) {
		this.eigenvectors = eigenvectors;
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
		clone.individuals = new ArrayList<Individual>(Properties.ARGUMENTS.get().getPopulationSize());
		clone.individuals.addAll(this.individuals);
		return clone;
	}

	public Object cloneAll() throws CloneNotSupportedException {
		Population clone = (Population) super.clone();
		clone.individuals = new ArrayList<Individual>(Properties.ARGUMENTS.get().getPopulationSize());
		for (Individual individual : this.individuals) {
			Individual individualClone = (Individual) individual.clone();
			clone.individuals.add(individualClone);
		}
		return clone;
	}

	public void write(File csvFile) throws IOException {
		int populationSize = this.size();
		int individualSize = this.get(0).size();

		StringBuffer sbFormat = new StringBuffer("%s");
		for (int j = 0; j < individualSize - 1; j++)
			sbFormat.append(",%s");
		sbFormat.append("\n");
		String format = sbFormat.toString();

		BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile));

		Object[] values = new Object[individualSize];
		for (int i = 0; i < populationSize; i++) {
			for (int j = 0; j < individualSize; j++)
				values[j] = this.get(i).get(j);
			String line = String.format(format, values);
			writer.write(line);
		}

		writer.close();
	}

	public void load(double[][] population) {
		this.individuals = new ArrayList<Individual>(population.length);
		double bestError = Double.MAX_VALUE;
		for (int index = 0; index < population.length; index++) {
			Individual individual = this.initializable.newInitialized();
			individual.setId(population[index]);
			this.individuals.add(individual);
			
			double error = Helper.getError(individual);
			if (index == 0 || error < bestError) {
				bestError = error;
				this.best = individual;
			}
		}
	}

	public void load(File csvFile) {
		Function<String, Individual> mapToItem = (line) -> {
			String[] arrayIdStr = line.split(",");
			double[] id = Arrays.stream(arrayIdStr).mapToDouble(Double::parseDouble).toArray();
			return this.initializable.newInitialized(id);
		};
		try {
			InputStream is = new FileInputStream(csvFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			this.individuals = br.lines().map(mapToItem).collect(Collectors.toList());

			double bestError = Double.MAX_VALUE;
			for (Individual individual : this.individuals) {
				double error = Helper.getError(individual);
				if (error < bestError) {
					bestError = error;
					this.best = individual;
				}
			}
			
			br.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}		
	}

	public double[][] toMatrix() {
		int populationSize = this.size();
		int individualSize = this.get(0).size();
		double[][] matrix = new double[populationSize][individualSize];
		for (int i = 0; i < populationSize; i++)
			for (int j = 0; j < individualSize; j++)
				matrix[i][j] = this.get(i).get(j);
		return matrix;
	}
}