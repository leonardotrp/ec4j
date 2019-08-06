package br.ufrj.coc.cec2015.algorithm;

import java.util.UUID;

import br.ufrj.coc.cec2015.util.Helper;

public class Individual implements Comparable<Individual>, Cloneable {
	private String uuid = UUID.randomUUID().toString();
	/**
	 * identification: sequência de valores que identificam um indivíduo (ex: similar ao cromossomo no AG)
	 */
	private double[] id;
	private double functionValue;
	
	// used in adaptative/self-adaptative DE
	private double differencialWeight; // Fi
	private double crossoverRate; // CRi
	private boolean F_flag, CR_flag; // DPADE: false=left | true=right
	
	// used in ABC
	private int trial = 0;
	private double fitness;
	private double probability;
	
	// used in PSO
	private double[] bestKnown; // local best
	private double functionValueBestKnow = Double.MAX_VALUE;
	private double[] velocity;
	
	// used in SRPSO
	private double inertiaWeight;
	
	// used in RIO
	private int hungerCount = 0;

	/**
	 * Construtor da classe Individuo
	 * @param size
	 */
	public Individual(int size) {
		super();
		this.initialize(size);
	}

	/**
	 * Construtor da classe Individuo
	 * @param id
	 */
	public Individual(double[] id) {
		super();
		this.id = id;
		this.bestKnown = this.id.clone();
	}
	
	public double[] getId() {
		return this.id;
	}
	
	public void setId(double[] id) {
		this.id = id;
	}
	
	public double get(int index) {
		return this.id[index];
	}
	
	public void set(int index, double value) {
		this.id[index] = value;
	}
	
	public int size() {
		return this.id.length;
	}
	
	private void initialize(int size) {
		this.id = new double[size];
		for (int index = 0; index < size; index++) {
			this.id[index] = Helper.randomData();
		}
		this.bestKnown = this.id.clone();
	}
	
	public double getFunctionValue() {
		return this.functionValue;
	}

	public void setFunctionValue(double fitness) {
		this.functionValue = fitness;
	}
	
	public double getDifferencialWeight() {
		return differencialWeight;
	}

	public void setDifferencialWeight(double differencialWeight) {
		this.differencialWeight = differencialWeight;
	}

	public double getCrossoverRate() {
		return crossoverRate;
	}

	public void setCrossoverRate(double crossoverRate) {
		this.crossoverRate = crossoverRate;
	}

	public boolean isF_flag() {
		return F_flag;
	}

	public void setF_flag(boolean f_flag) {
		F_flag = f_flag;
	}

	public boolean isCR_flag() {
		return CR_flag;
	}

	public void setCR_flag(boolean cR_flag) {
		CR_flag = cR_flag;
	}

	public int getTrial() {
		return trial;
	}

	public void setTrial(int trial) {
		this.trial = trial;
	}
	
	public void resetTrial() {
		this.trial = 0;
	}
	
	public void incrementTrial() {
		this.trial++;
	}

	public int getHungerCount() {
		return hungerCount;
	}

	public void setHungerCount(int hungerCount) {
		this.hungerCount = hungerCount;
	}
	
	public void incrementHungerCount() {
		this.hungerCount++;
	}
	
	public double getFitness() {
		return fitness;
	}

	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

	public double getProbability() {
		return probability;
	}

	public void setProbability(double probability) {
		this.probability = probability;
	}

	public double[] getBestKnown() {
		return bestKnown;
	}
	
	public double getBestKnown(int index) {
		return this.bestKnown[index];
	}

	public void setBestKnown(double[] best) {
		this.bestKnown = best;
	}

	public double getFunctionValueBestKnow() {
		return functionValueBestKnow;
	}

	public void setFunctionValueBestKnow(double functionValueBestKnow) {
		this.functionValueBestKnow = functionValueBestKnow;
	}

	public double[] getVelocity() {
		return velocity;
	}
	
	public double getVelocity(int index) {
		return this.velocity[index];
	}

	public void setVelocity(double[] velocity) {
		this.velocity = velocity;
	}
	
	public void setVelocity(int index, double velocity) {
		this.velocity[index] = velocity;
	}
	
	public double getInertiaWeight() {
		return inertiaWeight;
	}

	public void setInertiaWeight(double inertiaWeight) {
		this.inertiaWeight = inertiaWeight;
	}

	@Override
	public int compareTo(Individual o) {
		if (this.getFunctionValue() < o.getFunctionValue())
			return -1;
		else if (this.getFunctionValue() > o.getFunctionValue())
			return 1;
		return 0;
	}
	
	@Override
	public Individual clone() {
		Individual clone = new Individual(this.id.clone());
		clone.functionValue = this.functionValue;
		return clone;
	}
	
	@Override
	public boolean equals(Object obj) {
		Individual o = (Individual) obj;
		return this.uuid == o.uuid;
	}
}
