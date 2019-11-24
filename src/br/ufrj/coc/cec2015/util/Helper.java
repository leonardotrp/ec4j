package br.ufrj.coc.cec2015.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.math3.ml.distance.EuclideanDistance;

import br.ufrj.coc.cec2015.algorithm.Individual;
import br.ufrj.coc.cec2015.algorithm.Population;

public class Helper {
	
	public static boolean terminateRun(Population population) {
		if (population.size() > 0) {
			double errorValue = population.getBestError();
			population.setMinErrorValueFound(errorValue <= Properties.MIN_ERROR_VALUE);
			boolean minErrorReached = population.isMinErrorValueFound() && Properties.STOP_BY_MIN_ERROR;

			return minErrorReached || Properties.ARGUMENTS.get().isMaxFESReached() || Properties.ARGUMENTS.get().isMaxGenerationsReached();
		}
		return false;
	}

	private static Random random = new Random();

	public static double randomInRange(double min, double max) {
		double range = max - min;
		double scaled = random.nextDouble() * range;
		return scaled + min;
	}
	
	public static int randomInRange(int min, int max) {
		int randomIndex = ThreadLocalRandom.current().nextInt(min, max + 1);
		return randomIndex;
	}
	
	public static double randomData() {
		double[] range = Properties.getSearchRange();
		double random = randomInRange(range[0], range[1]);
		return random;
	}
	
	public static double checkLimits(double value) {
		double[] range = Properties.getSearchRange();
		if (value < range[0])
			return range[0];
		else if (value > range[1])
			return range[1];
		else
			return value;
	}
	
	public static double getError(Individual individual) {
		return getError(individual.getFunctionValue());
	}
	
	public static double getError(double functionValue) {
		double optimumValue = Properties.ARGUMENTS.get().getFunctionNumber() * 100;
		return Math.abs(optimumValue - functionValue);
	}
		
	public static Individual newIndividualInitialized() {
		Individual individual = new Individual(Properties.ARGUMENTS.get().getIndividualSize());
		double functionValue = Properties.ARGUMENTS.get().evaluateFunction(individual.getId());
		individual.setFunctionValue(functionValue);
		return individual;
	}

	public static Individual newIndividualInitialized(double[] id) {
		Individual individual = new Individual(id);
		double functionValue = Properties.ARGUMENTS.get().evaluateFunction(individual.getId());
		individual.setFunctionValue(functionValue);
		return individual;
	}

	public static double getFunctionValueDifference(Population population) {
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;
		for (Individual individual : population.getIndividuals()) {
			if (individual.getFunctionValue() < min)
				min = individual.getFunctionValue();
			if (individual.getFunctionValue() > max)
				max = individual.getFunctionValue();
		}
		return max - min;
	}
	
	public static double getMaxDistance(Population population) {
		double[] vMin = new double[Properties.ARGUMENTS.get().getIndividualSize()];
		double[] vMax = new double[Properties.ARGUMENTS.get().getIndividualSize()];
		for (int indexI = 0; indexI < Properties.ARGUMENTS.get().getIndividualSize(); indexI++) {
			vMin[indexI] = Double.MAX_VALUE;
			vMax[indexI] = -Double.MAX_VALUE;
		}
		for (int indexP = 0; indexP < population.getIndividuals().size(); indexP++) {
			Individual individual = population.get(indexP);
			for (int indexI = 0; indexI < individual.size(); indexI++) {
				double variable = individual.get(indexI);
				if (variable < vMin[indexI])
					vMin[indexI] = variable;
				else if (variable > vMax[indexI])
					vMax[indexI] = variable;
			}
		}
		return new EuclideanDistance().compute(vMin, vMax);
	}

	public static double getAverageLongs(List<Long> doubles) {
		LongSummaryStatistics stats = doubles.stream().mapToLong((x) -> x).summaryStatistics();
		return stats.getAverage();
	}

	public static double getAverageDoubles(List<Double> doubles) {
		DoubleSummaryStatistics stats = doubles.stream().mapToDouble((x) -> x).summaryStatistics();
		return stats.getAverage();
	}	

	public static double getAverageIntegers(List<Integer> integers) {
		IntSummaryStatistics stats = integers.stream().mapToInt((x) -> x).summaryStatistics();
		return stats.getAverage();
	}	
	
	public static List<Double> calculateErrors(Population population) {
		List<Double> errors = population.getIndividuals().stream().map(individual -> Helper.getError(individual)).collect(Collectors.toList());
		return errors;
	}
	
	public static double calculateMean(List<Double> numbers) {
		BigDecimal mean = new BigDecimal(0.0);
		for (Double number : numbers) {
			BigDecimal bdNumber = new BigDecimal(number);
			mean = mean.add(bdNumber);
		}
		mean = mean.divide(new BigDecimal(numbers.size()), 15, RoundingMode.HALF_UP);
		return mean.doubleValue();
	}

	public static double calculateMedian(List<Double> numbers) {
		Object[] values = numbers.toArray();
		Arrays.sort(values);
		int middleIndex = (int) ((values.length - 1) / 2);
		BigDecimal median;
		if (numbers.size() % 2 == 0) { // par
			median = new BigDecimal((double) values[middleIndex]);
			BigDecimal middleErrorPlus = new BigDecimal((double) values[middleIndex + 1]);
			median = median.add(middleErrorPlus).divide(new BigDecimal(2), 15, RoundingMode.HALF_UP);
		} else {
			median = new BigDecimal((double) values[middleIndex + 1]);
		}
		return median.doubleValue();
	}

	public static double calculateStandardDeviation(List<Double> numbers) {
		double mean = calculateMean(numbers);
		return calculateStandardDeviation(numbers, mean);
	}
	
	public static double calculateStandardDeviation(List<Double> numbers, double mean) {
		BigDecimal bdMean = new BigDecimal(mean);
		BigDecimal standardDeviation = new BigDecimal(0.0);
		for (Double error : numbers) {

			BigDecimal bdError = new BigDecimal(error);
			bdError = bdError.subtract(bdMean);
			bdError = bdError.pow(2);

			standardDeviation = bdMean.add(bdError);
		}
		double result = standardDeviation.divide(BigDecimal.valueOf(numbers.size() - 1), 15, RoundingMode.HALF_UP).doubleValue();
		return Math.sqrt(result);
	}

	public static double calculateLehmerMean(List<Double> numbers) {
		BigDecimal dividend = new BigDecimal(0.0);
		BigDecimal divisor = new BigDecimal(0.0);
		for (Double number : numbers) {
			BigDecimal bdNumber = new BigDecimal(number);
			dividend = dividend.add(bdNumber.pow(2));
			divisor = divisor.add(bdNumber);
		}
		return dividend.divide(divisor, 15, RoundingMode.HALF_UP).doubleValue();
	}

	public static double calculateWeightedMean(List<Double> numbers, List<Double> weights) {
		double totalWeights = weights.stream().mapToDouble(Double::doubleValue).sum();
		class BigDecimalWrapper {
			private BigDecimal value = new BigDecimal(0.0);
		}
		final BigDecimalWrapper bdWrapper = new BigDecimalWrapper();
		IntStream.range(0, numbers.size()).forEach(idx -> {
			// wFi,k
			BigDecimal weight = new BigDecimal(weights.get(idx));
			weight = weight.divide(new BigDecimal(totalWeights), 15, RoundingMode.HALF_UP);
			
			BigDecimal number = new BigDecimal(numbers.get(idx));
			bdWrapper.value = bdWrapper.value.add(weight.multiply(number));
		});
		return bdWrapper.value.doubleValue();
	}
}
