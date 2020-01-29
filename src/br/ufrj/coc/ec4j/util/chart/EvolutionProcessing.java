package br.ufrj.coc.ec4j.util.chart;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;

import br.ufrj.coc.ec4j.Main;
import br.ufrj.coc.ec4j.algorithm.Algorithm;
import br.ufrj.coc.ec4j.algorithm.AlgorithmArguments;
import br.ufrj.coc.ec4j.util.Helper;
import br.ufrj.coc.ec4j.util.Properties;

public class EvolutionProcessing {
	static final ResourceBundle bundle = ResourceBundle.getBundle(EvolutionProcessing.class.getPackage().getName() + ".chart");
	static boolean roundBestOfBests = Boolean.parseBoolean(bundle.getString("round_best_of_bests"));
	static boolean roundMeanOfBests = Boolean.parseBoolean(bundle.getString("round_mean_of_bests"));
	static boolean roundWorstOfBests = Boolean.parseBoolean(bundle.getString("round_worst_of_bests"));

	static boolean roundBestOfMeans = Boolean.parseBoolean(bundle.getString("round_best_of_means"));
	static boolean roundMeanOfMeans = Boolean.parseBoolean(bundle.getString("round_mean_of_means"));
	static boolean roundWorstOfMeans = Boolean.parseBoolean(bundle.getString("round_worst_of_means"));

	static boolean roundBestOfParams = Boolean.parseBoolean(bundle.getString("round_best_of_params"));
	static boolean roundMeanOfParams = Boolean.parseBoolean(bundle.getString("round_mean_of_params"));
	static boolean roundWorstOfParams = Boolean.parseBoolean(bundle.getString("round_worst_of_params"));

	static boolean meanOfBests = Boolean.parseBoolean(bundle.getString("mean_of_bests"));
	static boolean medianOfBests = Boolean.parseBoolean(bundle.getString("median_of_bests"));
	static boolean stdOfBests = Boolean.parseBoolean(bundle.getString("std_of_bests"));

	static boolean meanOfMeans = Boolean.parseBoolean(bundle.getString("mean_of_means"));
	static boolean medianOfMeans = Boolean.parseBoolean(bundle.getString("median_of_means"));
	static boolean stdOfMeans = Boolean.parseBoolean(bundle.getString("std_of_means"));

	public static int[] bestWorstRound(String file) throws IOException {
		String prefixBest = "Rodada Melhor";
		String prefixMean = "Rodada Intermediária";
		String prefixWorst = "Rodada Pior";
		BufferedReader br = null;
		int best = -1;
		int mean = -1;
		int worst = -1;
		try {
			br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.startsWith(prefixBest)) {
					line = line.split("\\|")[0].trim();
					best = Integer.parseInt(line.split("=")[1].trim());
				}
				if (line.startsWith(prefixMean)) {
					line = line.split("\\|")[0].trim();
					mean = Integer.parseInt(line.split("=")[1].trim());
				}
				if (line.startsWith(prefixWorst)) {
					line = line.split("\\|")[0].trim();
					worst = Integer.parseInt(line.split("=")[1].trim());
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

	private static boolean extracted(List<Double> listOfMaxFES, String fileRoundErrorsName, List<Double> listOfMeans, List<Double> listOfMedians, List<Double> listOfStds, int roundColumn, List<Double> roundErrors) throws IOException {
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
			        		if (roundErrors != null && round == (roundColumn - 1))
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
		            	if (listOfMedians != null) {
			                double median = Helper.calculateMedian(roundValues);
			                listOfMedians.add(median);
		            	}
		            	if (listOfStds != null) {
			                double std = Helper.calculateStandardDeviation(roundValues);
			                listOfStds.add(std);
		            	}
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
		String PATH_RESULT = "D:\\Google Drive (COC)\\trabalho de dissertação\\2 - jade with eig\\experimentos\\CR_JADE\\CR_JADE_R51_STUDY_NEW\\01";
		EvolutionChart2D bestRoundChart = null, meanRoundChart = null, worstRoundChart = null;
		EvolutionChart2D meanOfBestsChart = null, medianOfBestsChart = null, stdOfBestsChart = null;
		EvolutionChart2D meanOfMeansChart = null, medianOfMeansChart = null, stdOfMeansChart = null;

		try {
			for (int individualSize : Properties.INDIVIDUAL_SIZES) { // loop dimensions
	
		        // Write the output to a file
				File directory = new File(PATH_RESULT + "\\gráficos\\D" + individualSize);
				if (!directory.exists())
					directory.mkdirs();
	
				for (int functionNumber : Properties.FUNCTION_NUMBERS) { // loop functions
	
					bestRoundChart = null; meanRoundChart = null; worstRoundChart = null;
					meanOfBestsChart = null; medianOfBestsChart = null; stdOfBestsChart = null;
					meanOfMeansChart = null; medianOfMeansChart = null; stdOfMeansChart = null;

					int populationSize = 0;
					for (String algotithmName : Properties.ALGORITHMS) { // loop algorithms
	
						String className = Algorithm.class.getPackage().getName() + '.' + algotithmName.toLowerCase() + '.' + algotithmName;
						Algorithm algorithm = Main.newInstanceAlgorithm(className);
	
						for (String variant : algorithm.getVariants()) {  // loop variants
	
							AlgorithmArguments arguments = new AlgorithmArguments(algotithmName, variant, algorithm.getInfo(), functionNumber, individualSize);
							populationSize = arguments.getPopulationSize();
	
							String fileRoundErrorOfBestsName = PATH_RESULT + '\\' + algotithmName + '\\' + arguments.getPrefixFile() + "_evolution.csv";
							String fileRoundErrorOfMeansName = PATH_RESULT + '\\' + algotithmName + '\\' + arguments.getPrefixFile() + "_evolution_mean.csv";
							String fileRoundFeDiffsName = PATH_RESULT + '\\' + algotithmName + '\\' + arguments.getPrefixFile() + "_evolution_diff.csv";
							String fileRoundMaxDistsName = PATH_RESULT + '\\' + algotithmName + '\\' + arguments.getPrefixFile() + "_evolution_maxdist.csv";
							String fileRoundDetCovMatsName = PATH_RESULT + '\\' + algotithmName + '\\' + arguments.getPrefixFile() + "_evolution_detmatcov.csv";

							List<Double> listOfMaxFES = new ArrayList<Double>();

							List<Double> meanOfBestsErros = new ArrayList<Double>();
							List<Double> medianOfBestsErros = new ArrayList<Double>();
							List<Double> stdsOfBestsErros = new ArrayList<Double>();
							if (meanOfBests)
								extracted(listOfMaxFES, fileRoundErrorOfBestsName, meanOfBestsErros, medianOfBestsErros, stdsOfBestsErros, 0, null);

							List<Double> meanOfMeansErros = new ArrayList<Double>();
							List<Double> medianOfMeansErros = new ArrayList<Double>();
							List<Double> stdsOfMeansErros = new ArrayList<Double>();
							if (meanOfMeans)
								extracted(listOfMaxFES, fileRoundErrorOfMeansName, meanOfMeansErros, medianOfMeansErros, stdsOfMeansErros, 0, null);
							
							int[] bestWorstRoundColumn = bestWorstRound(fileRoundErrorOfBestsName);
							int bestRound = bestWorstRoundColumn[0];
							int meanRound = bestWorstRoundColumn[1];
							int worstRound = bestWorstRoundColumn[2];

							List<Double> bestRoundErros = new ArrayList<Double>();
							List<Double> meanRoundErros = new ArrayList<Double>();
							List<Double> worstRoundErros = new ArrayList<Double>();
							if (roundBestOfBests && extracted(listOfMaxFES, fileRoundErrorOfBestsName, null, null, null, bestRound, bestRoundErros)) {
								if (roundMeanOfBests)
									extracted(null, fileRoundErrorOfBestsName, null, null, null, meanRound, meanRoundErros);
								if (roundWorstOfBests)
									extracted(null, fileRoundErrorOfBestsName, null, null, null, worstRound, worstRoundErros);
							}

							List<Double> bestRoundErroMeans = new ArrayList<Double>();
							List<Double> meanRoundErroMeans = new ArrayList<Double>();
							List<Double> worstRoundErroMeans = new ArrayList<Double>();
							if (roundBestOfMeans && extracted(null, fileRoundErrorOfMeansName, null, null, null, bestRound, bestRoundErroMeans)) {
								if (roundMeanOfMeans)
									extracted(null, fileRoundErrorOfMeansName, null, null, null, meanRound, meanRoundErroMeans);
								if (roundWorstOfMeans)
									extracted(null, fileRoundErrorOfMeansName, null, null, null, worstRound, worstRoundErroMeans);
							}

							List<Double> bestRoundFeDiffs = new ArrayList<Double>();
							List<Double> meanRoundFeDiffs = new ArrayList<Double>();
							List<Double> worstRoundFeDiffs = new ArrayList<Double>();
							if (roundBestOfParams && extracted(null, fileRoundFeDiffsName, null, null, null, bestRound, bestRoundFeDiffs)) {
								if (roundMeanOfParams)
									extracted(null, fileRoundFeDiffsName, null, null, null, meanRound, meanRoundFeDiffs);
								if (roundWorstOfParams)
									extracted(null, fileRoundFeDiffsName, null, null, null, worstRound, worstRoundFeDiffs);
							}

							List<Double> bestRoundMaxDists = new ArrayList<Double>();
							List<Double> meanRoundMaxDists = new ArrayList<Double>();
							List<Double> worstRoundMaxDists = new ArrayList<Double>();
							if (roundBestOfParams && extracted(null, fileRoundMaxDistsName, null, null, null, bestRound, bestRoundMaxDists)) {
								if (roundMeanOfParams)
									extracted(null, fileRoundMaxDistsName, null, null, null, meanRound, meanRoundMaxDists);
								if (roundWorstOfParams)
									extracted(null, fileRoundMaxDistsName, null, null, null, worstRound, worstRoundMaxDists);
							}

							List<Double> bestRoundDetCovMats = new ArrayList<Double>();
							List<Double> meanRoundDetCovMats = new ArrayList<Double>();
							List<Double> worstRoundDetCovMats = new ArrayList<Double>();
							if (roundBestOfParams && extracted(null, fileRoundDetCovMatsName, null, null, null, bestRound, bestRoundDetCovMats)) {
								if (roundMeanOfParams)
									extracted(null, fileRoundDetCovMatsName, null, null, null, meanRound, meanRoundDetCovMats);
								if (roundWorstOfParams)
									extracted(null, fileRoundDetCovMatsName, null, null, null, worstRound, worstRoundDetCovMats);
							}

							
							String subTitle = String.format("F%d - D%d - P%d - R%d", functionNumber, individualSize, populationSize, Properties.MAX_RUNS);
							System.err.println(subTitle);
							
							// MEAN OF BESTS
							if (meanOfBests) {
								if (meanOfBestsChart == null) {
									meanOfBestsChart = new EvolutionChart2D("Média dos Melhores Erros", null);
									String titleMean = String.format("Evolução da Média dos Melhores Erros", Properties.MAX_RUNS);
									meanOfBestsChart.setTitle(titleMean, subTitle);
								}
								meanOfBestsChart.addSerie(listOfMaxFES, meanOfBestsErros, arguments.getTitleChart() + " ("+variant+")", 0);
							}
							
							// MEDIAN OF BESTS
							if (medianOfBests) {
								if (medianOfBestsChart == null) {
									medianOfBestsChart = new EvolutionChart2D("Mediana dos Melhores Erros", null);
									String titleMedian = String.format("Evolução da Mediana dos Melhores Erros", Properties.MAX_RUNS);
									medianOfBestsChart.setTitle(titleMedian, subTitle);
								}
								medianOfBestsChart.addSerie(listOfMaxFES, medianOfBestsErros, arguments.getTitleChart() + " ("+variant+")", 0);
							}

							// STD OF BESTS
							if (stdOfBests) {
								if (stdOfBestsChart == null) {
									stdOfBestsChart = new EvolutionChart2D("Desvio Padrão dos Melhores Erros", null);
									String titleStd = String.format("Evolução do Desvio Padrão dos Melhores Erros", Properties.MAX_RUNS);
									stdOfBestsChart.setTitle(titleStd, subTitle);
								}
								stdOfBestsChart.addSerie(listOfMaxFES, stdsOfBestsErros, arguments.getTitleChart() + " ("+variant+")", 0);
							}

							// MEAN OF MEANS
							if (meanOfMeans) {
								if (meanOfMeansChart == null) {
									meanOfMeansChart = new EvolutionChart2D("Média dos Erros Médios", null);
									String titleMean = String.format("Evolução da Média dos Erros Médios", Properties.MAX_RUNS);
									meanOfMeansChart.setTitle(titleMean, subTitle);
								}
								meanOfMeansChart.addSerie(listOfMaxFES, meanOfMeansErros, arguments.getTitleChart() + " ("+variant+")", 0);
							}
							
							// MEDIAN OF BESTS
							if (medianOfMeans) {
								if (medianOfMeansChart == null) {
									medianOfMeansChart = new EvolutionChart2D("Mediana dos Erros Médios", null);
									String titleMedian = String.format("Evolução da Mediana dos Erros Médios", Properties.MAX_RUNS);
									medianOfMeansChart.setTitle(titleMedian, subTitle);
								}
								medianOfMeansChart.addSerie(listOfMaxFES, medianOfMeansErros, arguments.getTitleChart() + " ("+variant+")", 0);
							}

							// STD OF MEANS
							if (stdOfMeans) {
								if (stdOfMeansChart == null) {
									stdOfMeansChart = new EvolutionChart2D("Desvio Padrão dos Erros Médios", null);
									String titleStd = String.format("Evolução do Desvio Padrão dos Erros Médios", Properties.MAX_RUNS);
									stdOfMeansChart.setTitle(titleStd, subTitle);
								}
								stdOfMeansChart.addSerie(listOfMaxFES, stdsOfMeansErros, arguments.getTitleChart() + " ("+variant+")", 0);
							}
							
							// BEST ROUND
							if (roundBestOfBests) {
								if (bestRoundChart == null) {
									bestRoundChart = new EvolutionChart2D("Evolução dos Erros (L)", "Evolução dos Parâmetros (R)");
									String titleBest = String.format("Melhor Rodada (%d): Evolução dos Erros (L) x Parâmetros (R)", bestRound);
									bestRoundChart.setTitle(titleBest, subTitle);
								}
								bestRoundChart.addSerie(listOfMaxFES, bestRoundErros, "Best (L)", 0);
								if (roundBestOfMeans)
									bestRoundChart.addSerie(listOfMaxFES, bestRoundErroMeans, "Mean (L)", 0);
								if (roundBestOfParams) {
									bestRoundChart.addSerie(listOfMaxFES, bestRoundFeDiffs, "Fmax - Fmin (R)", 1);
									bestRoundChart.addSerie(listOfMaxFES, bestRoundMaxDists, "Máx. Dist (R)", 1);
									if (bestRoundDetCovMats.size() > 0)
										bestRoundChart.addSerie(listOfMaxFES, bestRoundDetCovMats, "Det.MatCov (R)", 1);
								}
							}
							
							// MEAN ROUND
							if (roundMeanOfBests) {
								if (meanRoundChart == null) {
									meanRoundChart = new EvolutionChart2D("Evolução dos Erros (L)", "Evolução dos Parâmetros (R)");
									String titleMean = String.format("Rodada Média (%d): Evolução dos Erros (L) x Parâmetros (R)", meanRound);
									meanRoundChart.setTitle(titleMean, subTitle);
								}
								meanRoundChart.addSerie(listOfMaxFES, meanRoundErros, "Best (L)", 0);
								if (roundMeanOfMeans)
									meanRoundChart.addSerie(listOfMaxFES, meanRoundErroMeans, "Mean (L)", 0);
								if (roundMeanOfParams) {
									meanRoundChart.addSerie(listOfMaxFES, meanRoundFeDiffs, "Fmax - Fmin (R)", 1);
									meanRoundChart.addSerie(listOfMaxFES, meanRoundMaxDists, "Máx. Dist (R)", 1);
									if (meanRoundDetCovMats.size() > 0)
										meanRoundChart.addSerie(listOfMaxFES, meanRoundDetCovMats, "Det.MatCov (R)", 1);
								}
							}

							// WORST ROUND
							if (roundWorstOfBests) {
								if (worstRoundChart == null) {
									worstRoundChart = new EvolutionChart2D("Evolução dos Erros (L)", "Evolução dos Parâmetros (R)");
									String titleWorst = String.format("Pior Rodada (%d): Evolução dos Erros (L) x Parâmetros (R)", worstRound);
									worstRoundChart.setTitle(titleWorst, subTitle);
								}
								worstRoundChart.addSerie(listOfMaxFES, worstRoundErros, "Best (L)", 0);
								if (roundWorstOfMeans)
									worstRoundChart.addSerie(listOfMaxFES, worstRoundErroMeans, "Mean (L)", 0);
								if (roundWorstOfParams) {
									worstRoundChart.addSerie(listOfMaxFES, worstRoundFeDiffs, "Fmax - Fmin (R)", 1);
									worstRoundChart.addSerie(listOfMaxFES, worstRoundMaxDists, "Máx. Dist (R)", 1);
									if (worstRoundDetCovMats.size() > 0)
										worstRoundChart.addSerie(listOfMaxFES, worstRoundDetCovMats, "Det.MatCov (R)", 1);
								}
							}
						}
					}

					if (meanOfBests) {
						File directoryMeanOfBests = new File(directory.getAbsolutePath() + "\\mean_of_bests");
						if (!directoryMeanOfBests.exists())
							directoryMeanOfBests.mkdirs();
						String meanOfBestsFilePng = directoryMeanOfBests.getAbsolutePath() + String.format("\\P%d_F%d_D%d_evolution_mean_of_bests.png", populationSize, functionNumber, individualSize);
						meanOfBestsChart.toFile(meanOfBestsFilePng);
					}
					if (medianOfBests) {
						File directoryMedianOfBests = new File(directory.getAbsolutePath() + "\\median_of_bests");
						if (!directoryMedianOfBests.exists())
							directoryMedianOfBests.mkdirs();
						String medianOfBestsFilePng = directoryMedianOfBests.getAbsolutePath() + String.format("\\P%d_F%d_D%d_evolution_median_of_bests.png", populationSize, functionNumber, individualSize);
						medianOfBestsChart.toFile(medianOfBestsFilePng);
					}
					if (stdOfBests) {
						File directoryStdOfBests = new File(directory.getAbsolutePath() + "\\std_of_bests");
						if (!directoryStdOfBests.exists())
							directoryStdOfBests.mkdirs();
						String stdOfBestsFilePng = directoryStdOfBests.getAbsolutePath() + String.format("\\P%d_F%d_D%d_evolution_std_of_bests.png", populationSize, functionNumber, individualSize);
						stdOfBestsChart.toFile(stdOfBestsFilePng);
					}
					if (meanOfMeans) {
						File directoryMeanOfMeans = new File(directory.getAbsolutePath() + "\\mean_of_means");
						if (!directoryMeanOfMeans.exists())
							directoryMeanOfMeans.mkdirs();
						String meanOfMeansFilePng = directoryMeanOfMeans.getAbsolutePath() + String.format("\\P%d_F%d_D%d_evolution_mean_of_means.png", populationSize, functionNumber, individualSize);
						meanOfMeansChart.toFile(meanOfMeansFilePng);
					}
					if (medianOfMeans) {
						File directoryMedianOfMeans = new File(directory.getAbsolutePath() + "\\median_of_means");
						if (!directoryMedianOfMeans.exists())
							directoryMedianOfMeans.mkdirs();
						String medianOfMeansFilePng = directoryMedianOfMeans.getAbsolutePath() + String.format("\\P%d_F%d_D%d_evolution_median_of_means.png", populationSize, functionNumber, individualSize);
						medianOfMeansChart.toFile(medianOfMeansFilePng);
					}
					if (stdOfMeans) {
						File directoryStdOfMeans = new File(directory.getAbsolutePath() + "\\std_of_means");
						if (!directoryStdOfMeans.exists())
							directoryStdOfMeans.mkdirs();
						String stdOfMeansFilePng = directoryStdOfMeans.getAbsolutePath() + String.format("\\P%d_F%d_D%d_evolution_std_of_means.png", populationSize, functionNumber, individualSize);
						stdOfMeansChart.toFile(stdOfMeansFilePng);
					}
					if (roundBestOfBests) {
						File directoryBest = new File(directory.getAbsolutePath() + "\\best_round");
						if (!directoryBest.exists())
							directoryBest.mkdirs();
						String bestFilePng = directoryBest.getAbsolutePath() + String.format("\\P%d_F%d_D%d_evolution_best.png", populationSize, functionNumber, individualSize);
						bestRoundChart.toFile(bestFilePng);
					}
					if (roundMeanOfBests) {
						File directoryMean = new File(directory.getAbsolutePath() + "\\mean_round");
						if (!directoryMean.exists())
							directoryMean.mkdirs();
						String meanFilePng = directoryMean.getAbsolutePath() + String.format("\\P%d_F%d_D%d_evolution_mean.png", populationSize, functionNumber, individualSize);
						meanRoundChart.toFile(meanFilePng);
					}
					if (roundWorstOfBests) {
						File directoryWorst = new File(directory.getAbsolutePath() + "\\worst_round");
						if (!directoryWorst.exists())
							directoryWorst.mkdirs();
						String worstFilePng = directoryWorst.getAbsolutePath() + String.format("\\P%d_F%d_D%d_evolution_worst.png", populationSize, functionNumber, individualSize);
						worstRoundChart.toFile(worstFilePng);
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (meanOfBestsChart != null)
				meanOfBestsChart.close();
			if (medianOfBestsChart != null)
				medianOfBestsChart.close();
			if (stdOfBestsChart != null)
				stdOfBestsChart.close();
			if (meanOfMeansChart != null)
				meanOfMeansChart.close();
			if (medianOfMeansChart != null)
				medianOfMeansChart.close();
			if (stdOfMeansChart != null)
				stdOfMeansChart.close();
			if (bestRoundChart != null)
				bestRoundChart.close();
			if (meanRoundChart != null)
				meanRoundChart.close();
			if (worstRoundChart != null)
				worstRoundChart.close();
		}
	}
}