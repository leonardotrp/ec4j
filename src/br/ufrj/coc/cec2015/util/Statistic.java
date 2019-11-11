package br.ufrj.coc.cec2015.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import javax.swing.JFrame;

import Jama.Matrix;
import br.ufrj.coc.cec2015.algorithm.Individual;
import br.ufrj.coc.cec2015.algorithm.Population;
import br.ufrj.coc.cec2015.util.chart.ProjectionChart2D;
import br.ufrj.coc.cec2015.util.chart.ProjectionChart2D.ProjectionData;

public class Statistic {
	static private String ID = UUID.randomUUID().toString();
	private AtomicInteger counter = new AtomicInteger();
	private ProjectionChart2D projectionChart2D;

	private BufferedWriter fileRoundErrors;
	private BufferedWriter fileEvolutionOfErrors;
	private List<Double> roundErros = new ArrayList<Double>(Properties.MAX_RUNS);
	private List<Long> timeRounds = new ArrayList<Long>(Properties.MAX_RUNS);
	private static double[] EVALUATION_LIMITS = new double[] {
			0.001, 0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09,
			0.10, 0.11, 0.12, 0.13, 0.14, 0.15, 0.16, 0.17, 0.18, 0.19,
			0.20, 0.21, 0.22, 0.23, 0.24, 0.25, 0.26, 0.27, 0.28, 0.29,
			0.30, 0.31, 0.32, 0.33, 0.34, 0.35, 0.36, 0.37, 0.38, 0.39,
			0.40, 0.41, 0.42, 0.43, 0.44, 0.45, 0.46, 0.47, 0.48, 0.49,
			0.50, 0.51, 0.52, 0.53, 0.54, 0.55, 0.56, 0.57, 0.58, 0.59,
			0.60, 0.61, 0.62, 0.63, 0.64, 0.65, 0.66, 0.67, 0.68, 0.69,
			0.70, 0.71, 0.72, 0.73, 0.74, 0.75, 0.76, 0.77, 0.78, 0.79,
			0.80, 0.81, 0.82, 0.83, 0.84, 0.85, 0.86, 0.87, 0.88, 0.89,
			0.90, 0.91, 0.92, 0.93, 0.94, 0.95, 0.96, 0.97, 0.98, 0.99,
			1.00 };

	class ErrorEvolution {
		private Integer round;
		private Double error;
		public ErrorEvolution(Population population) {
			super();
			this.error = population.getBestError();
		}
		public Double getError() {
			return error;
		}
		public Integer getRound() {
			return round;
		}
		public void setRound(Integer round) {
			this.round = round;
		}
	}

	private Map<Integer, List<ErrorEvolution>> errorEvolution; // <round number, lista de erro em cada rodada para cada instante definido>
	private int successfulRuns;

	public Statistic() {
		super();
		start();
		initializeProjection();
	}

	private void initializeProjection() {
		if (Properties.USE_PROJECTIONS) {
			this.projectionChart2D = new ProjectionChart2D("Projection Chart 2D");
			this.projectionChart2D.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.projectionChart2D.pack();
			this.projectionChart2D.setLocationRelativeTo(null);
			this.projectionChart2D.setVisible(Properties.SHOW_PROJECTIONS);
		}
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
			this.initializeFunction();

			String algorithmName = Properties.ARGUMENTS.get().getName();
			String prefixFile = Properties.ARGUMENTS.get().getPrefixFile();

			String fileRoundErrorsName = FileUtil.getFileName(ID, algorithmName, prefixFile + "_statistics.csv");
			this.fileRoundErrors = new BufferedWriter(new FileWriter(fileRoundErrorsName));
			writeHeadStatistics(this.fileRoundErrors);

			String fileEvolutionOfErrorsName = FileUtil.getFileName(ID, algorithmName, prefixFile + "_evolution.csv");
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
		List<ErrorEvolution> roundErrors = errorEvolution.get(round);
		if (roundErrors == null) {
			roundErrors = new ArrayList<ErrorEvolution>(EVALUATION_LIMITS.length);
			errorEvolution.put(round, roundErrors);
		}
		for (int indexEvaluation = 0; indexEvaluation < EVALUATION_LIMITS.length; indexEvaluation++) {
			int evaluationValue = (int) (EVALUATION_LIMITS[indexEvaluation] * Properties.ARGUMENTS.get().getMaxFES());
			if (countEvaluations == evaluationValue) {
				if (indexEvaluation > 0 && indexEvaluation > roundErrors.size()) {
					ErrorEvolution lastErrorEvolution = (roundErrors.size() > 0) ? roundErrors.get(roundErrors.size() - 1) : null;
					for (int index = roundErrors.size(); index < indexEvaluation; index++)
						roundErrors.add(index, lastErrorEvolution);
				}
				roundErrors.add(indexEvaluation, new ErrorEvolution(population));
				if (Properties.isUpdateProjectionsInstant())
					updateProjections2D(round, population);
			}
		}
		if (Properties.isUpdateProjectionsAll())
			updateProjections2D(round, population);
	}

	private void updateProjections2D(int round, Population population) {
		// 1ª coluna: 1º autovetor (x axis)
		// 2ª coluna: 2º autovetor (y axis)
		// demais colunas: projeção p = (x, y)
		Matrix eigenvectors = population.getFirstEigenvectors();

		if (eigenvectors == null || !Properties.USE_PROJECTIONS)
			return;

		int individualSize = Properties.ARGUMENTS.get().getIndividualSize();
		double[][] projectionsSeries = new double[population.size()][2];
		int bestIndex = 0;
		for (int idxIndividual = 0; idxIndividual < population.size(); idxIndividual++) {
			Individual individual = population.get(idxIndividual);
			double x = 0, y = 0;
			for (int idxId = 0; idxId < individualSize; idxId++) {
				x += eigenvectors.get(idxId, 0) * individual.get(idxId);
				y += eigenvectors.get(idxId, 1) * individual.get(idxId);
			}
			projectionsSeries[idxIndividual] = new double[] { x, y };
			if (population.getBest() == individual)
				bestIndex = idxIndividual;
		}

		// atualiza as projeções no plano
		ProjectionData projectionData = this.projectionChart2D.new ProjectionData();
		projectionData.setSeries(projectionsSeries);
		projectionData.setBest(bestIndex);
		projectionData.setTitle("Projeções no plano formado pelas componentes principais V1 e V2");
		projectionData.getSubTitles().add(Properties.ARGUMENTS.get().getName() + " - Função F" + Properties.ARGUMENTS.get().getFunctionNumber() + " - Dimensão " + Properties.ARGUMENTS.get().getIndividualSize());
		projectionData.getSubTitles().add("Rodada (" + (round + 1) + ")");
		if (Properties.EXPORT_PROJECTIONS) {
			String projectionsPath = Properties.ARGUMENTS.get().getName() + "/projections/F" + Properties.ARGUMENTS.get().getFunctionNumber() + "/" + round;
			String pngChartFilename = FileUtil.getFileName(ID, projectionsPath, "projection_round" + round + "_" + this.counter.incrementAndGet() + ".png");
			projectionData.setPngFilename(pngChartFilename);
		}

		this.projectionChart2D.update(projectionData);
	}

	private void writeHeadEvolutionOfErrors() throws IOException {
		StringBuffer sbFormat = new StringBuffer("%-30s");
		Object[] head = new String[Properties.MAX_RUNS + 2];
		head[0] = "MaxFES";
		for (int round = 0; round < Properties.MAX_RUNS; round++) {
			sbFormat.append(", %-22s");
			head[round + 1] = "R" + (round + 1);
		}
		sbFormat.append(", %-22s\n");
		head[Properties.MAX_RUNS + 1] = "Mean";
		String headLine = String.format(sbFormat.toString(), head);

		this.fileEvolutionOfErrors.write(headLine);
	}

	private void writeLineEvolutionOfErrors(Object[] errorValues) throws IOException {
		StringBuffer sbFormat = new StringBuffer("%-30s");
		for (int round = 0; round < Properties.MAX_RUNS + 1; round++) {
			sbFormat.append(", %-22s");
		}
		sbFormat.append('\n');

		String errorLine = String.format(sbFormat.toString(), errorValues);
		this.fileEvolutionOfErrors.write(errorLine);
	}

	private static String formatNumber(Double value) {
		DecimalFormat df = new DecimalFormat("0.000000000000000E0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
		return df.format(value); // 1.23456789E4
	}

	private ErrorEvolution getBestRound() {
		int minErrorsCount = Integer.MAX_VALUE;
		double minimum = Double.MAX_VALUE;
		ErrorEvolution best = null;
		for (int round = 1; round <= Properties.MAX_RUNS; round++) {
			List<ErrorEvolution> roundErrors = errorEvolution.get(round - 1);
			int countErros = (int) roundErrors.stream().filter(error -> error != null).count();
			ErrorEvolution minRoundErrorEvolution = roundErrors.stream().filter(error -> error != null).min(Comparator.comparing(ErrorEvolution::getError)).get();
			if (countErros < minErrorsCount) {
				minErrorsCount = countErros;
				minimum = minRoundErrorEvolution.getError();
				best = minRoundErrorEvolution;
				best.setRound(round);
			} else if (countErros == minErrorsCount && minRoundErrorEvolution.getError() < minimum) {
				minimum = minRoundErrorEvolution.getError();
				best = minRoundErrorEvolution;
				best.setRound(round);
			}
		}
		return best;
	}

	private void writeEvolutionOfErros() throws IOException {
		writeHeadEvolutionOfErrors();
		for (int indexEvaluation = 0; indexEvaluation < EVALUATION_LIMITS.length; indexEvaluation++) {
			BigDecimal errorMean = new BigDecimal(0.0);
			Object[] attrValues = new Object[Properties.MAX_RUNS + 2];
			attrValues[0] = EVALUATION_LIMITS[indexEvaluation];
			for (int round = 1; round <= Properties.MAX_RUNS; round++) {
				List<ErrorEvolution> roundErros = errorEvolution.get(round - 1);
				attrValues[round] = "-";
				if (indexEvaluation < roundErros.size()) {
					ErrorEvolution errorEvolution = roundErros.get(indexEvaluation);
					if (errorEvolution != null) {
						errorMean = errorMean.add(new BigDecimal(errorEvolution.getError()));
						attrValues[round] = formatNumber(errorEvolution.getError());
					}
				}
			}
			errorMean = errorMean.divide(new BigDecimal(Properties.MAX_RUNS), 15, RoundingMode.HALF_UP);
			String strErrorMean = errorMean.doubleValue() > 0 ? formatNumber(errorMean.doubleValue()) : "-";
			attrValues[Properties.MAX_RUNS + 1] = strErrorMean; 
			writeLineEvolutionOfErrors(attrValues);
		}
		ErrorEvolution bestRound = getBestRound();
		StringBuffer resume = new StringBuffer();
		resume.append("\nInformação: " + Properties.ARGUMENTS.get().getInfo());
		resume.append("\nNúmero de Avaliações: " + Properties.ARGUMENTS.get().getCountEvaluations());
		resume.append("\nNúmero de Gerações: " + Properties.ARGUMENTS.get().getCountGenerations());
		resume.append("\nMelhor Rodada: " + bestRound.getRound());
		resume.append("\nMenor Erro: " + bestRound.getError());
		this.fileEvolutionOfErrors.write(resume.toString());
	}

	private void writeHeadStatistics(BufferedWriter writer) throws IOException {
		String strFormat = "%-10s, %-22s, %-22s, %-22s, %-22s, %-22s, %-10s, %-10s, %-10s\n";
		String head = String.format(strFormat + '\n', "FUNCTION", "BEST", "WORST", "MEDIAN", "MEAN", "STD", "POPSIZE", "TIME(s)", "SR");
		writer.write(head);
		System.out.println(head);
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
		System.err.println(line);
	}

	public void addRound(Population population) throws IOException {
		List<Double> errors = calculateErrors(population);
		Long timeElapsed = this.getTimeElapsed(this.initialTimeRound);
		this.timeRounds.add(timeElapsed);
		this.writeLineStatistic(this.fileRoundErrors, "F(" + Properties.ARGUMENTS.get().getFunctionNumber() + "):Round(" + (this.roundErros.size() + 1) + ")", errors, timeElapsed, null);
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

		if (this.projectionChart2D != null)
			this.projectionChart2D.close();
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
