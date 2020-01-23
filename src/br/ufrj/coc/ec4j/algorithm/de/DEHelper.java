package br.ufrj.coc.ec4j.algorithm.de;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.distribution.CauchyDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import Jama.Matrix;
import br.ufrj.coc.ec4j.algorithm.BaseAlgorithmHelper;
import br.ufrj.coc.ec4j.algorithm.Individual;
import br.ufrj.coc.ec4j.algorithm.Population;
import br.ufrj.coc.ec4j.algorithm.de.DEProperties.EigRateAdaptation;
import br.ufrj.coc.ec4j.math.MatrixUtil;
import br.ufrj.coc.ec4j.util.Helper;
import br.ufrj.coc.ec4j.util.Properties;

public class DEHelper extends BaseAlgorithmHelper {
	private Population sortedPopulation;
	private double[] cumulativeVector;
	private List<Integer> populationIndexes;
	private DEProperties properties;
	private Matrix eigenvectors;

	public DEHelper() {
		super();
		this.properties = new DEProperties();
		this.eigenvectors = null;
		Properties.ARGUMENTS.get().resetPopulationSize();
	}
	
	@Override
	public void initializeGeneration(Population population) {
		super.initializeGeneration(population);
		this.initializeCumulativeVector();
		this.initializePopulationIndexes();
		this.initializeEigOperator(population);
	}
	
	private void initializeEigOperator(Population population) {
		boolean useProjections = Properties.USE_PROJECTIONS && population.getFirstEigenvectors() == null;
		
		if (this.isUseEig() || useProjections) {
			Matrix cm = MatrixUtil.getCovarianceMatrix(population);
			population.setDetMatCov(cm.det());
			
			//this.increasePopulation(population, cm.det());
			this.eigenvectors = cm.eig().getV();

			if (useProjections) // save the first eigenvectors
				population.setFirstEigenvectors(this.eigenvectors);
		}
	}

	protected boolean isUseEig() {
		return this.properties.isEigenvectorCrossover();
	}
	
	public DEProperties getProperties() {
		return this.properties;
	}

	public Population getSortedPopulation() {
		if (this.sortedPopulation == null)
			initializeSortedPopulation();
		return this.sortedPopulation;
	}

	public List<Integer> getPopulationIndexes() {
		return this.populationIndexes;
	}

	// --------------------------------------------------------------------------------
	// --------- CUMULATIVE VECTOR
	// ----------------------------------------------------
	// --------------------------------------------------------------------------------
	private void initializeCumulativeVector() {
		cumulativeVector = new double[super.getPopulation().size()];
		cumulativeVector[0] = super.getPopulation().size();
		cumulativeVector[1] = cumulativeVector[0] + cumulativeVector[0] * 0.8;
		for (int index = 2; index < super.getPopulation().size(); index++) {
			cumulativeVector[index] = cumulativeVector[index - 1]
					+ (cumulativeVector[index - 1] - cumulativeVector[index - 2]) * 0.8;
		}
	}

	// --------------------------------------------------------------------------------
	// --------- POPULATION INDEXES
	// ---------------------------------------------------
	// --------------------------------------------------------------------------------
	private void initializePopulationIndexes() {
		if (this.populationIndexes == null || this.populationIndexes.size() != super.getPopulation().size())
			this.populationIndexes = new ArrayList<Integer>(super.getPopulation().size()); // lista com os índices da
																						// população, para nunca
																						// selecionar um índice
																						// repetido desnecessariamente
		this.populationIndexes.clear();
		for (int index = 0; index < super.getPopulation().size(); index++) {
			this.populationIndexes.add(index);
		}
	}

	private int randomPopulationIndex() {
		int index = Helper.randomInRange(0, this.populationIndexes.size() - 1);
		int populationIndex = this.populationIndexes.get(index);
		this.populationIndexes.remove(index);
		return populationIndex;
	}

	protected void removePopulationIndex(int populationIndex) {
		if (this.populationIndexes.contains(populationIndex)) {
			int index = this.populationIndexes.indexOf(populationIndex);
			this.populationIndexes.remove(index);
		}
	}

	public boolean canCrossover(Individual current) {
		boolean rouletteAll = this.properties.isRouletteAllStrategy();
		double crossoverRate = rouletteAll ? Helper.randomInRange(current.getCrossoverRate(), 1.0)
				: current.getCrossoverRate();
		return Math.random() <= crossoverRate;
	}

	protected void initializeSortedPopulation() {
		try {
			this.sortedPopulation = (Population) super.getPopulation().clone();
			Collections.sort(this.sortedPopulation.getIndividuals());
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	private int rouletteWheel() {
		double pin = Helper.randomInRange(0.0, 1.0) * this.cumulativeVector[this.sortedPopulation.size() - 1];
		int indexResult = 0;
		for (int index = 0; index < super.getPopulation().size(); index++) {
			if (this.cumulativeVector[index] >= pin) {
				indexResult = index;
				break;
			}
		}
		return indexResult;
	}

	private Individual spinRoulette() {
		int drwanIndex = rouletteWheel();
		return this.sortedPopulation.remove(drwanIndex);
	}

	public Individual selectDestiny() {
		if (this.properties.isCurrentToStrategy()) {
			if (this.properties.isCurrentToBestStrategy())
				return super.getPopulation().getBest();
			else if (this.properties.isCurrentToRandStrategy())
				return super.getPopulation().get(randomPopulationIndex());
			else if (this.properties.isCurrentToPBestStrategy()) {
				int indexTop = selectPBestIndex(DEProperties.GREEDINESS);
				return this.getSortedPopulation().get(indexTop);
			}
		}
		return null;
	}

	private int selectPBestIndex(double greediness) {
		BigDecimal p = new BigDecimal(greediness);
		// BigDecimal percentTop = p.multiply(BigDecimal.valueOf(100)); // 100 . p%
		int indexLastTop = p.multiply(BigDecimal.valueOf(Properties.ARGUMENTS.get().getPopulationSize())).intValue();
		int indexTop = indexLastTop == 0 ? 0 : Helper.randomInRange(0, indexLastTop);
		return indexTop;
	}
	
	private Individual selectBase(Individual current) {
		Individual base = null;
		if (this.properties.isBestStrategy()) {
			base = super.getPopulation().getBest();
		} else if (this.properties.isCurrentToStrategy()) {
			base = current;
		} else if (this.properties.isRandStrategy()) {
			base = super.getPopulation().get(randomPopulationIndex());
		} else if (this.properties.isRouletteStrategy()) {
			base = spinRoulette();
		}
		return base;
	}

	protected Individual getIndividualX2() {
		return super.getPopulation().get(randomPopulationIndex());
	}

	private List<Individual> selectPartners() {
		boolean rouletteSelection = this.properties.isRouletteStrategyOthers();
		List<Individual> partners = new ArrayList<Individual>();

		partners.add(rouletteSelection ? spinRoulette() : super.getPopulation().get(randomPopulationIndex())); // X1

		Individual X2 = this.getIndividualX2();
		partners.add(X2); // X2

		if (this.properties.getMutationDifferenceCount() > 1) {
			partners.add(rouletteSelection ? spinRoulette() : super.getPopulation().get(randomPopulationIndex())); // X3
			partners.add(rouletteSelection ? spinRoulette() : super.getPopulation().get(randomPopulationIndex())); // X4
		}

		return partners;
	}

	private double mutate(int j, double differencialWeight, Individual base, Individual destiny,
			List<Individual> partners) {
		BigDecimal xBaseG = new BigDecimal(base.get(j)), mutatedValue = xBaseG;
		BigDecimal F = new BigDecimal(differencialWeight), difference;

		if (destiny != null) { // 'DE/current-to-{rand/best/pbest}/N'
			BigDecimal K = this.properties.isCurrentToRandStrategy() ? new BigDecimal(Helper.randomInRange(0.0, 1.0))
					: F;
			BigDecimal xDestiny = new BigDecimal(destiny.get(j));
			difference = xDestiny.subtract(xBaseG);
			mutatedValue = mutatedValue.add(K.multiply(difference));
		}

		int index = 0;
		while (index < partners.size()) {
			BigDecimal x1G = new BigDecimal(partners.get(index++).get(j));
			BigDecimal x2G = new BigDecimal(partners.get(index++).get(j));

			difference = x1G.subtract(x2G);
			mutatedValue = mutatedValue.add(F.multiply(difference));
		}

		return Helper.checkLimits(mutatedValue.doubleValue());
	}

	private double getDifferencialWeight(Individual current) {
		boolean rouletteOthers = this.properties.isRouletteStrategyOthers();
		return rouletteOthers ? Helper.randomInRange(current.getDifferencialWeight(), 1.0)
				: current.getDifferencialWeight();
	}

	private double[] generateTrialVectorBin(Individual current, Individual base, Individual destiny,
			List<Individual> partners) {
		double differencialWeight = getDifferencialWeight(current);
		int individualSize = Properties.ARGUMENTS.get().getIndividualSize();
		double[] trialVector = new double[individualSize]; /* Vi */
		for (int j = 0; j < individualSize; j++) {
			int randI = Helper.randomInRange(0, individualSize - 1);

			if (canCrossover(current) || j == randI) {
				trialVector[j] = mutate(j, differencialWeight, base, destiny, partners);
			} else {
				trialVector[j] = current.get(j);
			}
		}
		return trialVector; // Ui,G+1
	}

	private double[] generateTrialVectorEig(Individual current, Individual base, Individual destiny,
			List<Individual> partners) {
		double differencialWeight = getDifferencialWeight(current);
		int individualSize = Properties.ARGUMENTS.get().getIndividualSize();

		double[] X = current.getId();
		double[] V = new double[individualSize]; /* Vi */
		for (int j = 0; j < individualSize; j++) {
			V[j] = mutate(j, differencialWeight, base, destiny, partners);
		}

		Matrix Q = this.eigenvectors; // Qg

		double[] QX = new double[individualSize];
		double[] QV = new double[individualSize];
		for (int j = 0; j < individualSize; j++) {
			QX[j] = 0;
			QV[j] = 0;
			for (int k = 0; k < individualSize; k++) {
				QX[j] += Q.get(k, j) * X[k]; /* Qg.Xi */
				QV[j] += Q.get(k, j) * V[k]; /* Qg.Vi */
			}
		}

		double[] QU = new double[individualSize]; /* xover(Qg.Xi, Qg.Vi) */
		for (int j = 0; j < individualSize; j++) {
			int randI = Helper.randomInRange(0, individualSize - 1);
			if (canCrossover(current) || j == randI) {
				QU[j] = QV[j];
			} else {
				QU[j] = QX[j];
			}
		}

		RealMatrix QT = new Array2DRowRealMatrix(this.eigenvectors.getArray()).transpose(); // Qg*

		double[] trialVector = new double[individualSize];
		for (int j = 0; j < individualSize; j++) {
			trialVector[j] = 0;
			for (int k = 0; k < individualSize; k++) {
				trialVector[j] += QT.getEntry(k, j) * QU[k]; /* Qg* . xover(Qg.Xi, Qg.Vi) */
			}
			trialVector[j] = Helper.checkLimits(trialVector[j]);
		}
		return trialVector; // Ui,G+1
	}

	protected double getEigRate() {
		double eigRate = DEProperties.EIG_RATE;
		if (DEProperties.EIG_RATE_ADAPTATION != null) { // default 0 ... 1
			BigDecimal factor = new BigDecimal(Properties.ARGUMENTS.get().getCountEvaluations());
			factor = factor.divide(new BigDecimal(Properties.ARGUMENTS.get().getMaxFES()), 5, RoundingMode.HALF_UP);
			if (DEProperties.EIG_RATE_ADAPTATION.equals(EigRateAdaptation.DESC))
				factor = BigDecimal.valueOf(1).subtract(factor).setScale(5); // 1 ... 0
			eigRate = factor.doubleValue();
		}
		return eigRate;
	}

	public double[] generateTrialVector(Individual current /* Xi,G */) {
		this.initializePopulationIndexes();
		initializeSortedPopulation();

		Individual base = this.selectBase(current); // XBase
		removePopulationIndex(super.getPopulation().indexOf(base));

		List<Individual> partners = this.selectPartners(); // X1, X2, X3, X4
		Individual destiny = this.selectDestiny(); // strategy 'DE/current-to-{rand/best/pbest}/N'

		if (this.isUseEig() && Math.random() <= getEigRate())
			return generateTrialVectorEig(current, base, destiny, partners);
		else
			return generateTrialVectorBin(current, base, destiny, partners);
	}

	public void addInferior(Individual inferior) {
		// nothing
	}

	public void generateControlParameters(Individual individual) {
		individual.setCrossoverRate(DEProperties.CROSSOVER_RATE);
		individual.setDifferencialWeight(DEProperties.DIFFERENTIAL_WEIGHT);
	}

	public void addSuccessful(Individual successful) {
		// nothing
	}

	public void finalizeGeneration() {
		// nothing
	}

	/**
	 * At each generation g, the crossover probability CRi of each individual xi is
	 * independently generated according to a normal distribution of mean μCR and
	 * standard deviation 0.1
	 * 
	 * @return double
	 */
	protected double generateCrossoverRate(double mean_CR, double min, double max) {
		double standardDeviation = 0.1;
		// generate crossover rate to individual
		NormalDistribution d = new NormalDistribution(mean_CR, standardDeviation);
		double cr = d.sample();
		return (cr < min) ? min : (cr > max) ? max : cr;
	}

	/**
	 * At each generation g, the mutation factor Fi of each individual xi is
	 * independently generated according to a Cauchy distribution with location
	 * parameter μF and scale parameter 0.1
	 * 
	 * @return double
	 */
	protected double generateDifferencialWeight(double location_F, double min, double max) {
		double scale = 0.1;
		CauchyDistribution c = new CauchyDistribution(location_F, scale);
		double f = c.sample();
		return (f > max) ? max : (f <= min) ? this.generateDifferencialWeight(location_F, min, max) : f;
	}
}
