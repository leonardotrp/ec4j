package br.ufrj.coc.cec2015.algorithm;

import br.ufrj.coc.cec2014.functions.testfunc;
//import br.ufrj.coc.base.functions.testfunc;
import br.ufrj.coc.cec2015.util.Properties;

public class AlgorithmArguments {
	private testfunc functions = new testfunc();
	
	private String name;
	private String variant;
	private String info;
	private int functionNumber;
	private int populationSize;
	private int individualSize;
	private int countEvaluations;
	private int countGenerations;
	private int maxFES;

	public AlgorithmArguments(String name, String variant, String info, int functionNumber, int individualSize) {
		super();

		this.name = name;
		this.variant = variant;
		this.info = info;
		this.functionNumber = functionNumber;
		this.populationSize = Properties.POPULATION_SIZES.get(name);
		this.individualSize = individualSize;
		this.maxFES = 10000 * individualSize;

		try {
			this.functions.loadConstants(individualSize, functionNumber);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String getName() {
		return name;
	}

	public String getVariant() {
		return variant;
	}

	public String getInfo() {
		return info;
	}

	public int getFunctionNumber() {
		return functionNumber;
	}

	public int getPopulationSize() {
		return populationSize;
	}

	public void setPopulationSize(int populationSize) {
		this.populationSize = populationSize;
	}

	public int getIndividualSize() {
		return individualSize;
	}

	public int getCountEvaluations() {
		return countEvaluations;
	}

	public int getCountGenerations() {
		return countGenerations;
	}

	public void initialize() {
		this.countEvaluations = 0;
		this.countGenerations = 0;
	}

	public void incrementCountEvaluations() {
		this.countEvaluations++;
	}

	public void incrementCountGenerations() {
		this.countGenerations++;
	}
	
	public int getMaxFES() {
		return maxFES;
	}

	public void setMaxFES(int maxFES) {
		this.maxFES = maxFES;
	}

	public double evaluateFunction(double[] input) {
		double functionValue = 0.0;
		try {
			functionValue = this.functions.exec_func(input, input.length, this.functionNumber);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		this.incrementCountEvaluations();
		return functionValue;
	}

	public boolean isMaxFESReached() {
		return Properties.STOP_BY_MAX_FES && this.countEvaluations >= this.maxFES;
	}

	public boolean isMaxGenerationsReached() {
		return Properties.STOP_BY_MAX_GEN && this.countGenerations >= Properties.MAX_GENERATIONS;
	}
	
	public double getEvolutionFactor() {
		return 1 - (this.countEvaluations / this.maxFES);
	}
	
	public String getPrefixFile() {
		return this.name + '_' + this.variant.replace('/', '.') + "_P" + this.populationSize + "_F" + this.functionNumber + "_D" + this.individualSize;
	}

	public String getTitleChart() {
		return this.name + '_' + this.variant.replace('/', '.') + "_P" + this.populationSize + "_F" + this.functionNumber + "_D" + this.individualSize;
	}
}
