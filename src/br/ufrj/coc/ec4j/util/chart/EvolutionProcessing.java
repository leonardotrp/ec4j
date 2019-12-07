package br.ufrj.coc.ec4j.util.chart;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import br.ufrj.coc.ec4j.Main;
import br.ufrj.coc.ec4j.algorithm.Algorithm;
import br.ufrj.coc.ec4j.algorithm.AlgorithmArguments;
import br.ufrj.coc.ec4j.util.Helper;
import br.ufrj.coc.ec4j.util.Properties;

public class EvolutionProcessing {
	public static int[] bestWorstRound(String file) throws IOException {
		String prefixBest = "Rodada Melhor: ";
		String prefixMean = "Rodada Intermediária: ";
		String prefixWorst = "Rodada Pior: ";
		BufferedReader br = null;
		int best = -1;
		int mean = -1;
		int worst = -1;
		try {
			br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.startsWith(prefixBest)) {
					best = Integer.parseInt(line.split(":")[1].trim());
				}
				if (line.startsWith(prefixMean)) {
					mean = Integer.parseInt(line.split(":")[1].trim());
				}
				if (line.startsWith(prefixWorst)) {
					worst = Integer.parseInt(line.split(":")[1].trim());
				}
            }
		} catch (FileNotFoundException e) {
			System.err.println("File not found: " + file);
		} finally {
			if (br != null)
				br.close();
		}
		return new int[] {best, mean, worst};
	}

	private static boolean extracted(List<Double> listOfMaxFES, List<Double> listOfMeans, String fileRoundErrorsName, int roundColumn, List<Double> roundErrors) throws IOException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fileRoundErrorsName));
			String line;
		    double maxFES = 0.001;
			while ((line = br.readLine()) != null && maxFES < 1) {

				List<Double> roundValues = new ArrayList<>(Properties.MAX_RUNS);
				
		        // use comma as separator
		        Scanner columns = new Scanner(line.trim());
		        columns.useDelimiter("\\s*,\\s*");
		        
		        if (columns.hasNextDouble()) {
		        	maxFES = columns.nextDouble();

		        	int round = 0;
		        	boolean hasValue = false;
		        	while (round < Properties.MAX_RUNS) {
		        		Double roundValue = Properties.MIN_ERROR_VALUE;
		        		if (columns.hasNextDouble()) {
		        			hasValue = true;
		        			roundValue = columns.nextDouble();
			        		if (round == (roundColumn - 1))
			        			roundErrors.add(roundValue);
		        		}
		        		else
		        			columns.next();
		        		roundValues.add(roundValue);
		        		round++;
		        	}
		            columns.close();

		            if (hasValue) {
		            	if (listOfMaxFES != null)
		            		listOfMaxFES.add(maxFES);
		                
		            	if (listOfMeans != null) {
			                double mean = Helper.calculateMean(roundValues);
			                listOfMeans.add(mean);
		            	}
		                /*
		                double median = Helper.calculateMedian(roundValues);
		                listOfMedians.add(median);
		                */
		            }
		        }
		    }
			return true;
		} catch (FileNotFoundException e) {
			System.err.println("File not found: " + fileRoundErrorsName);
			return false;
		} finally {
			if (br != null)
				br.close();
		}
	}
	
	public static void main(String[] args) throws Exception {
		String PATH_RESULT = "D:\\Google Drive (COC)\\trabalho de dissertação\\2 - jade with eig\\experimentos\\CR_JADE_R10_ROUNDS_STUDY";
		EvolutionChart2D bestRoundChart = null, meanRoundChart = null, worstRoundChart = null;

		try {
			for (int individualSize : Properties.INDIVIDUAL_SIZES) { // loop dimensions
	
		        // Write the output to a file
				File directory = new File(PATH_RESULT + "\\gráficos\\D" + individualSize);
				if (!directory.exists())
					directory.mkdirs();
	
				for (int functionNumber : Properties.FUNCTION_NUMBERS) { // loop functions
	
					bestRoundChart = null; meanRoundChart = null; worstRoundChart = null;
					int populationSize = 0;
					for (String algotithmName : Properties.ALGORITHMS) { // loop algorithms
	
						String className = Algorithm.class.getPackage().getName() + '.' + algotithmName.toLowerCase() + '.' + algotithmName;
						Algorithm algorithm = Main.newInstanceAlgorithm(className);
	
						for (String variant : algorithm.getVariants()) {  // loop variants
	
							AlgorithmArguments arguments = new AlgorithmArguments(algotithmName, variant, algorithm.getInfo(), functionNumber, individualSize);
							populationSize = arguments.getPopulationSize();
	
							String fileRoundErrorOfBestsName = PATH_RESULT + '\\' + algotithmName + '\\' + arguments.getPrefixFile() + "_evolution.csv";
							int[] bestWorstRoundColumn = bestWorstRound(fileRoundErrorOfBestsName);
							int bestRound = bestWorstRoundColumn[0];
							int meanRound = bestWorstRoundColumn[1];
							int worstRound = bestWorstRoundColumn[2];
	
							List<Double> listOfMaxFES = new ArrayList<Double>();
							List<Double> bestRoundErros = new ArrayList<Double>();
							extracted(listOfMaxFES, null, fileRoundErrorOfBestsName, bestRound, bestRoundErros);
							List<Double> meanRoundErros = new ArrayList<Double>();
							extracted(null, null, fileRoundErrorOfBestsName, meanRound, meanRoundErros);
							List<Double> worstRoundErros = new ArrayList<Double>();
							extracted(null, null, fileRoundErrorOfBestsName, worstRound, worstRoundErros);

							String fileRoundErrorOfMeansName = PATH_RESULT + '\\' + algotithmName + '\\' + arguments.getPrefixFile() + "_evolution_mean.csv";
							List<Double> bestRoundErroMeans = new ArrayList<Double>();
							extracted(null, null, fileRoundErrorOfMeansName, bestRound, bestRoundErroMeans);
							List<Double> meanRoundErroMeans = new ArrayList<Double>();
							extracted(null, null, fileRoundErrorOfMeansName, meanRound, meanRoundErroMeans);
							List<Double> worstRoundErroMeans = new ArrayList<Double>();
							extracted(null, null, fileRoundErrorOfMeansName, worstRound, worstRoundErroMeans);

							String fileRoundFeDiffsName = PATH_RESULT + '\\' + algotithmName + '\\' + arguments.getPrefixFile() + "_evolution_diff.csv";
							List<Double> bestRoundFeDiffs = new ArrayList<Double>();
							extracted(null, null, fileRoundFeDiffsName, bestRound, bestRoundFeDiffs);
							List<Double> meanRoundFeDiffs = new ArrayList<Double>();
							extracted(null, null, fileRoundFeDiffsName, meanRound, meanRoundFeDiffs);
							List<Double> worstRoundFeDiffs = new ArrayList<Double>();
							extracted(null, null, fileRoundFeDiffsName, worstRound, worstRoundFeDiffs);

							String fileRoundMaxDistsName = PATH_RESULT + '\\' + algotithmName + '\\' + arguments.getPrefixFile() + "_evolution_maxdist.csv";
							List<Double> bestRoundMaxDists = new ArrayList<Double>();
							extracted(null, null, fileRoundMaxDistsName, bestRound, bestRoundMaxDists);
							List<Double> meanRoundMaxDists = new ArrayList<Double>();
							extracted(null, null, fileRoundMaxDistsName, meanRound, meanRoundMaxDists);
							List<Double> worstRoundMaxDists = new ArrayList<Double>();
							extracted(null, null, fileRoundMaxDistsName, worstRound, worstRoundMaxDists);

							String fileRoundDetCovMatsName = PATH_RESULT + '\\' + algotithmName + '\\' + arguments.getPrefixFile() + "_evolution_detmatcov.csv";
							List<Double> bestRoundDetCovMats = new ArrayList<Double>();
							List<Double> meanRoundDetCovMats = new ArrayList<Double>();
							List<Double> worstRoundDetCovMats = new ArrayList<Double>();
							if (extracted(null, null, fileRoundDetCovMatsName, bestRound, bestRoundDetCovMats)) {
								extracted(null, null, fileRoundDetCovMatsName, meanRound, meanRoundDetCovMats);
								extracted(null, null, fileRoundDetCovMatsName, worstRound, worstRoundDetCovMats);
							}
							
							String subTitle = String.format("%s - Função %d - Dimensão %d - NP %d", arguments.getTitleChart(), functionNumber, individualSize, populationSize);
							System.err.println(subTitle);
							// BEST ROUND
							if (bestRoundChart == null) {
								bestRoundChart = new EvolutionChart2D("Evolução dos Erros (L)", "Evolução dos Parâmetros (R)");
								String titleBest = String.format("Melhor Rodada (%d): Evolução dos Erros (L) x Parâmetros (R)", bestRound);
								bestRoundChart.setTitle(titleBest, subTitle);
							}
							bestRoundChart.addSerie(listOfMaxFES, bestRoundErros, "Best (L)", 0);
							bestRoundChart.addSerie(listOfMaxFES, bestRoundErroMeans, "Mean (L)", 0);
							bestRoundChart.addSerie(listOfMaxFES, bestRoundFeDiffs, "Fmax - Fmin (R)", 1);
							bestRoundChart.addSerie(listOfMaxFES, bestRoundMaxDists, "Máx. Dist (R)", 1);
							if (bestRoundDetCovMats.size() > 0)
								bestRoundChart.addSerie(listOfMaxFES, bestRoundDetCovMats, "Det.MatCov (R)", 1);
							
							// MEAN ROUND
							if (meanRoundChart == null) {
								meanRoundChart = new EvolutionChart2D("Evolução dos Erros (L)", "Evolução dos Parâmetros (R)");
								String titleMean = String.format("Rodada Média (%d): Evolução dos Erros (L) x Parâmetros (R)", meanRound);
								meanRoundChart.setTitle(titleMean, subTitle);
							}
							meanRoundChart.addSerie(listOfMaxFES, meanRoundErros, "Best (L)", 0);
							meanRoundChart.addSerie(listOfMaxFES, meanRoundErroMeans, "Mean (L)", 0);
							meanRoundChart.addSerie(listOfMaxFES, meanRoundFeDiffs, "Fmax - Fmin (R)", 1);
							meanRoundChart.addSerie(listOfMaxFES, meanRoundMaxDists, "Máx. Dist (R)", 1);
							if (meanRoundDetCovMats.size() > 0)
								meanRoundChart.addSerie(listOfMaxFES, meanRoundDetCovMats, "Det.MatCov (R)", 1);

							// WORST ROUND
							if (worstRoundChart == null) {
								worstRoundChart = new EvolutionChart2D("Evolução dos Erros (L)", "Evolução dos Parâmetros (R)");
								String titleWorst = String.format("Pior Rodada (%d): Evolução dos Erros (L) x Parâmetros (R)", worstRound);
								worstRoundChart.setTitle(titleWorst, subTitle);
							}
							worstRoundChart.addSerie(listOfMaxFES, worstRoundErros, "Best (L)", 0);
							worstRoundChart.addSerie(listOfMaxFES, worstRoundErroMeans, "Mean (L)", 0);
							worstRoundChart.addSerie(listOfMaxFES, worstRoundFeDiffs, "Fmax - Fmin (R)", 1);
							worstRoundChart.addSerie(listOfMaxFES, worstRoundMaxDists, "Máx. Dist (R)", 1);
							if (worstRoundDetCovMats.size() > 0)
								worstRoundChart.addSerie(listOfMaxFES, worstRoundDetCovMats, "Det.MatCov (R)", 1);
							
							/*
							// MEAN
							if (meanEvolution == null) {
								meanEvolution = new EvolutionChart2D("Média dos " + label);
								String titleMean = String.format("Evolução da Média dos " + label + " (%d rodadas)", Properties.MAX_RUNS);
								meanEvolution.setTitle(titleMean, subTitle);
							}
							meanEvolution.addSerie(listOfMaxFES, listOfMeans, arguments.getTitleChart());
							
							// MEDIAN
							if (medianEvolution == null) {
								medianEvolution = new EvolutionChart2D("Mediana dos " + label);
								String titleMedian = String.format("Evolução da Mediana dos " + label + " (%d rodadas)", Properties.MAX_RUNS);
								medianEvolution.setTitle(titleMedian, subTitle);
							}
							medianEvolution.addSerie(listOfMaxFES, listOfMedians, arguments.getTitleChart());
							*/
						}
					}
					File directoryBest = new File(directory.getAbsolutePath() + "\\best_round");
					if (!directoryBest.exists())
						directoryBest.mkdirs();
					String bestFilePng = directoryBest.getAbsolutePath() + String.format("\\P%d_F%d_D%d_evolution_best.png", populationSize, functionNumber, individualSize);
					bestRoundChart.toFile(bestFilePng);

					File directoryMean = new File(directory.getAbsolutePath() + "\\mean_round");
					if (!directoryMean.exists())
						directoryMean.mkdirs();
					String meanFilePng = directoryMean.getAbsolutePath() + String.format("\\P%d_F%d_D%d_evolution_mean.png", populationSize, functionNumber, individualSize);
					meanRoundChart.toFile(meanFilePng);
					
					File directoryWorst = new File(directory.getAbsolutePath() + "\\worst_round");
					if (!directoryWorst.exists())
						directoryWorst.mkdirs();
					String worstFilePng = directoryWorst.getAbsolutePath() + String.format("\\P%d_F%d_D%d_evolution_worst.png", populationSize, functionNumber, individualSize);
					worstRoundChart.toFile(worstFilePng);
					/*
					File directoryMean = new File(directory.getAbsolutePath() + "\\mean");
					if (!directoryMean.exists())
						directoryMean.mkdirs();
					String meanFilePng = directoryMean.getAbsolutePath() + String.format("\\P%d_F%d_D%d_mean_evolution" + sufix + ".png", populationSize, functionNumber, individualSize);
					meanEvolution.toFile(meanFilePng);
					
					File directoryMedian = new File(directory.getAbsolutePath() + "\\median");
					if (!directoryMedian.exists())
						directoryMedian.mkdirs();
					String medianFilePng = directoryMedian.getAbsolutePath() + String.format("\\P%d_F%d_D%d_median_evolution" + sufix + ".png", populationSize, functionNumber, individualSize);
					medianEvolution.toFile(medianFilePng);
					*/
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (bestRoundChart != null)
				bestRoundChart.close();
			if (meanRoundChart != null)
				meanRoundChart.close();
			if (worstRoundChart != null)
				worstRoundChart.close();
		}
	}
}