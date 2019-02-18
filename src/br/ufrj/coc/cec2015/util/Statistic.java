package br.ufrj.coc.cec2015.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.UUID;

import br.ufrj.coc.cec2015.algorithm.Individual;
import br.ufrj.coc.cec2015.algorithm.Population;

public class Statistic {
	static private String ID = UUID.randomUUID().toString();

	private BufferedWriter fileRoundErrors;
	private BufferedWriter fileEvolutionOfErrors;
	private List<Double> roundErros = new ArrayList<Double>(Properties.MAX_RUNS);
	private List<Long> timeRounds = new ArrayList<Long>(Properties.MAX_RUNS);
	private static double[] EVALUATION_LIMITS = new double[] { 
		0.000001, 
		0.00001, 
		0.0001, 
		0.001, 
		0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 
		0.10, 0.11, 0.12, 0.13, 0.14, 0.15, 0.16, 0.17, 0.18, 0.19, 
		0.20, 0.21, 0.22, 0.23, 0.24, 0.25, 0.26, 0.27, 0.28, 0.29, 
		0.30, 0.31, 0.32, 0.33, 0.34, 0.35, 0.36, 0.37, 0.38, 0.39,
		0.40, 0.41, 0.42, 0.43, 0.44, 0.45, 0.46, 0.47, 0.48, 0.49,
		0.50, 0.51, 0.52, 0.53, 0.54, 0.55, 0.56, 0.57, 0.58, 0.59,
		0.60,
		0.7,
		0.8, 
		0.9, 
		1.0
	};
	private Map<Integer, List<Double>> errorEvolution; // <round number, lista de erro em cada rodada para cada instante definido>
	private int successfulRuns;
	
	public Statistic() {
		super();
		start();
	}

	private String getFileName(String relativePath, String filename) {
		URI uri;
		try {
			String root = Properties.RESULTS_ROOT + ID + '/';
			uri = new URI(root + relativePath);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		File directory = new File(uri);
		if (!directory.exists())
			directory.mkdirs();
		return directory.getAbsolutePath() + '\\' + filename;
	}

	private long initialTimeFunction, initialTimeRound;
	private void startTimeFunction() {
		this.initialTimeFunction = Instant.now().toEpochMilli();
	}
	private void startTimeRound() {
		this.initialTimeRound = Instant.now().toEpochMilli();
	}
	public long getTimeElapsed(long initialTime) {
		 return Instant.now().toEpochMilli() - initialTime;
	}
	private static double timeInSeconds(double time) {
		 return (double) time / (double) 1000;
	}
	
	private void initializeFunction() {
		this.timeRounds.clear();
		this.roundErros.clear();
		this.startTimeFunction();
		this.errorEvolution = new HashMap<>();
		this.successfulRuns = 0;
	}

	public void start() {
		try {
			System.out.println("::::::::::::::: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()) + " :::::::::::::");
			this.initializeFunction();
			
			String algorithmName = Properties.ARGUMENTS.get().getName();
			String prefixFile = Properties.ARGUMENTS.get().getPrefixFile();
			
			String fileRoundErrorsName = getFileName(algorithmName, prefixFile + "_statistics.csv");
			this.fileRoundErrors = new BufferedWriter(new FileWriter(fileRoundErrorsName));
			writeHeadStatistics(this.fileRoundErrors);

			String fileEvolutionOfErrorsName = getFileName(algorithmName, prefixFile + "_evolution.csv");
			this.fileEvolutionOfErrors = new BufferedWriter(new FileWriter(fileEvolutionOfErrorsName));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void startRound() {
		this.startTimeRound();
	}
	
	public void verifyEvaluationInstant(int round, Population population) {
		int countEvaluations = Properties.ARGUMENTS.get().getCountEvaluations();
		List<Double> roundErrors = errorEvolution.get(round);
		if (roundErrors == null) {
			roundErrors = new ArrayList<Double>(EVALUATION_LIMITS.length);
			errorEvolution.put(round, roundErrors);
		}
		for (int indexEvaluation = 0; indexEvaluation < EVALUATION_LIMITS.length; indexEvaluation++) {
			int evaluationValue = (int) (EVALUATION_LIMITS[indexEvaluation] * Properties.ARGUMENTS.get().getMaxFES());			
			if (countEvaluations == evaluationValue) {
				if (indexEvaluation > 0 && indexEvaluation > roundErrors.size()) {
					for (int index = roundErrors.size(); index < indexEvaluation; index++)
						roundErrors.add(index, null);
				}
				roundErrors.add(indexEvaluation, population.getBestError());
			}
		}
	}
	
	private void writeHeadEvolutionOfErrors() throws IOException {
		StringBuffer sbFormat = new StringBuffer("%-30s");
		Object[] head = new String[Properties.MAX_RUNS + 2];
		head[0] = "MaxFES";
		for (int round = 0; round < Properties.MAX_RUNS; round++) {
			sbFormat.append(", %-22s");
			head[round + 1] = "R" + (round + 1);
		}
		sbFormat.append(", %-22s\n\n");
		head[Properties.MAX_RUNS + 1] = "Mean";
		String headLine = String.format(sbFormat.toString(), head);
		
		this.fileEvolutionOfErrors.write(headLine);
		System.err.println(headLine);
	}

	private void writeLineEvolutionOfErrors(Object[] values) throws IOException {
		StringBuffer sbFormat = new StringBuffer("%-30s");
		for (int round = 0; round < Properties.MAX_RUNS + 1; round++) {
			sbFormat.append(", %-22s");
		}
		sbFormat.append('\n');
		String line = String.format(sbFormat.toString(), values);
		this.fileEvolutionOfErrors.write(line);
		System.out.println(line);
	}
	
	private static String formatNumber(Double value) {
		DecimalFormat df = new DecimalFormat("0.000000000000000E0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
	    return df.format(value); // 1.23456789E4		
	}
	
	private int getBestRound() {
		int bestRound = 0;
		int minErrorsCount = Integer.MAX_VALUE;
		double minimum = Double.MAX_VALUE;
		for (int round = 1; round <= Properties.MAX_RUNS; round++) {
			List<Double> roundErrors = errorEvolution.get(round - 1);
			int countErros = (int) roundErrors.stream().filter(error -> error != null).count();
			Double minRoundError = roundErrors.stream().filter(error -> error != null).min(Comparator.comparing(Double::valueOf)).get();
			if (countErros < minErrorsCount) {
				minErrorsCount = countErros;
				bestRound = round;
				minimum = minRoundError;
			}
			else if (countErros == minErrorsCount && minRoundError < minimum) {
				bestRound = round;
				minimum = minRoundError;
			}
		}
		return bestRound;
	}
	
	private void writeEvolutionOfErros() throws IOException {
		writeHeadEvolutionOfErrors();
		for (int indexEvaluation = 0; indexEvaluation < EVALUATION_LIMITS.length; indexEvaluation++) {
			BigDecimal mean = new BigDecimal(0.0);
			Object[] values = new Object[Properties.MAX_RUNS + 2];
			values[0] = EVALUATION_LIMITS[indexEvaluation];
			for (int round = 1; round <= Properties.MAX_RUNS; round++) {
				List<Double> roundErros = errorEvolution.get(round - 1);
				values[round] = "-";
				if (indexEvaluation < roundErros.size()) {
					Double error = roundErros.get(indexEvaluation);
					if (error != null) {
						mean = mean.add(new BigDecimal(error));
						values[round] = formatNumber(error);
					}
				}
			}
			mean = mean.divide(new BigDecimal(Properties.MAX_RUNS), 15, RoundingMode.HALF_UP);
			values[Properties.MAX_RUNS + 1] = mean.doubleValue() > 0 ? formatNumber(mean.doubleValue()) : "-";

			writeLineEvolutionOfErrors(values);
		}
		int bestRound = getBestRound();
		this.fileEvolutionOfErrors.write("\nBest Round = " + bestRound);
		System.out.println("\nBest Round = " + bestRound);
	}

	private void writeHeadStatistics(BufferedWriter writer) throws IOException {
		String strFormat = "%-10s, %-22s, %-22s, %-22s, %-22s, %-22s, %-10s, %-10s, %-10s\n";
		String head = String.format(strFormat + '\n', "FUNCTION", "BEST", "WORST", "MEDIAN", "MEAN", "STD", "POPSIZE", "TIME(s)", "SR");
		writer.write(head);
		System.err.println(head);
	}

	private void writeLineStatistic(BufferedWriter writer, String label, List<Double> errors, Long timeSpent, Double successfulRate) throws IOException {
		String strSuccessfulRate = "";
		if (successfulRate != null)
			strSuccessfulRate = formatNumber(successfulRate);
		
		Collections.sort(errors);

		String best = formatNumber(errors.get(0));
		String worst = formatNumber(errors.get(errors.size() - 1));
		String median = formatNumber(calculateMedian(errors));
		double mean = calculateMean(errors);
		String meanStr = formatNumber(mean);
		String standardDeviation = formatNumber(calculateStandardDeviation(errors, mean));
		
		String strFormat = "%-10s, %-22s, %-22s, %-22s, %-22s, %-22s, %-10s, %-10s, %-10s\n";
		String line = String.format(strFormat, label, best, worst, median, meanStr, standardDeviation, Properties.ARGUMENTS.get().getPopulationSize(), timeInSeconds(timeSpent), strSuccessfulRate);
		writer.write(line);
		System.out.println(line);
	}

	public void addRound(Population population) throws IOException {
		List<Double> errors = calculateErrors(population);
		Long timeElapsed = this.getTimeElapsed(this.initialTimeRound);
		this.timeRounds.add(timeElapsed);
		this.writeLineStatistic(this.fileRoundErrors, "Round(" + (this.roundErros.size() + 1) + ")", errors, timeElapsed, null);
		this.roundErros.add(population.getBestError());
		if (population.isMinErrorValueFound())
			this.successfulRuns++;
	}
	
	private double getAvgTimeSpentRound() {
		LongSummaryStatistics stats = this.timeRounds.stream().mapToLong((x) -> x).summaryStatistics();
		return (double) stats.getAverage();
	}

	public void close() throws IOException {
		double successfulRate = (double) this.successfulRuns / Properties.MAX_RUNS;
		long avgTimeSpent = (long) this.getAvgTimeSpentRound();

		this.writeLineStatistic(this.fileRoundErrors, "F(" + Properties.ARGUMENTS.get().getFunctionNumber() + ")", this.roundErros, avgTimeSpent, successfulRate);
		long timeElapsedFunction = this.getTimeElapsed(this.initialTimeFunction);		
		this.fileRoundErrors.write("\nTotal time = " + timeInSeconds(timeElapsedFunction));
		this.fileRoundErrors.close();

		this.writeEvolutionOfErros();
		this.fileEvolutionOfErrors.close();
		System.out.println("::::::::::::::: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()) + " :::::::::::::");
	}
	
	static List<Double> calculateErrors(Population population) {
		List<Double> errors = new ArrayList<Double>(population.size());
		for (Individual individual : population.getIndividuals()) {
			double error = Helper.getError(individual);
			errors.add(error);
		}
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
		int middleIndex = (int) ((numbers.size() - 1) / 2);
		BigDecimal median;
		if (numbers.size() % 2 == 0) { // par
			median = new BigDecimal(numbers.get(middleIndex));
			BigDecimal middleErrorPlus = new BigDecimal(numbers.get(middleIndex + 1));
			median = median.add(middleErrorPlus).divide(new BigDecimal(2));
		}
		else {
			median = new BigDecimal(numbers.get(middleIndex + 1));
		}
		return median.doubleValue();
	}
	
	static double calculateStandardDeviation(List<Double> errors, double meanOfErrors) {
		BigDecimal mean = new BigDecimal(meanOfErrors);
		BigDecimal standardDeviation = new BigDecimal(0.0);
		for (Double error : errors) {

			BigDecimal bdError = new BigDecimal(error);
			bdError = bdError.subtract(mean);
			bdError = bdError.pow(2);
			
			standardDeviation = mean.add(bdError);
		}
		double result = standardDeviation.divide(BigDecimal.valueOf(errors.size() - 1), 15, RoundingMode.HALF_UP).doubleValue();
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
}
