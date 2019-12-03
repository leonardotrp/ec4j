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
	public static int bestRound(String file) throws IOException {
		String prefix = "Melhor Rodada: ";
		BufferedReader br = null;
		int best = -1;
		try {
			br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.startsWith(prefix)) {
					best = Integer.parseInt(line.split(":")[1].trim());
				}
            }
		} catch (FileNotFoundException e) {
			System.err.println("File not found: " + file);
		} finally {
			if (br != null)
				br.close();
		}
		return best;
	}

	private static void extracted(List<Double> listOfMaxFES, List<Double> listOfBests, List<Double> listOfMeans, String fileRoundErrorsName, int bestRoundColumn) throws IOException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fileRoundErrorsName));
			String line;
		    double maxFES = 0.001;
			while ((line = br.readLine()) != null && maxFES < 1) {

				List<Double> roundValues = new ArrayList<>(Properties.MAX_RUNS);
				
		        // use comma as separator
		        Scanner columns = new Scanner(line);
		        columns.useDelimiter("\\s*,\\s*");
		        
		        if (columns.hasNextDouble()) {
		        	maxFES = columns.nextDouble();

		        	int round = 0;
		        	boolean hasValue = false;
		        	while (++round <= Properties.MAX_RUNS) {
		        		Double roundValue = Properties.MIN_ERROR_VALUE;
		        		if (columns.hasNextDouble()) {
		        			hasValue = true;
		        			roundValue = columns.nextDouble();
			        		if (round == bestRoundColumn)
			        			listOfBests.add(roundValue);
		        		}
		        		else
		        			columns.next();
		        		roundValues.add(roundValue);
		        	}
		            columns.close();

		            if (hasValue) {
		            	if (listOfMaxFES != null)
		            		listOfMaxFES.add(maxFES);
		                
		                double mean = Helper.calculateMean(roundValues);
		                listOfMeans.add(mean);
		                /*
		                double median = Helper.calculateMedian(roundValues);
		                listOfMedians.add(median);
		                */
		            }
		        }
		    }
		} catch (FileNotFoundException e) {
			System.err.println("File not found: " + fileRoundErrorsName);
		} finally {
			if (br != null)
				br.close();
		}
	}
	
	public static void main(String[] args) throws Exception {
		String PATH_RESULT = "D:\\Google Drive (COC)\\trabalho de dissertação\\2 - jade with eig\\experimentos\\JADE_EIG_ERRORDIFF_MAXDIST_STUDY\\CR_JADE_EIG_R10";
		String sufix = "";//"_maxdist";//"_diff";
		//String label = "Erros";//"Máxima Distância";//"(Fmax - Fmin)";
		EvolutionChart2D bestEvolution = null;//, meanEvolution = null;//, medianEvolution = null;

		try {
			for (int individualSize : Properties.INDIVIDUAL_SIZES) { // loop dimensions
	
		        // Write the output to a file
				File directory = new File(PATH_RESULT + "\\gráficos\\D" + individualSize);
				if (!directory.exists())
					directory.mkdirs();
	
				for (int functionNumber : Properties.FUNCTION_NUMBERS) { // loop functions
	
					bestEvolution = null; //meanEvolution = null;// medianEvolution = null;
					int populationSize = 0;
					for (String algotithmName : Properties.ALGORITHMS) { // loop algorithms
	
						String className = Algorithm.class.getPackage().getName() + '.' + algotithmName.toLowerCase() + '.' + algotithmName;
						Algorithm algorithm = Main.newInstanceAlgorithm(className);
	
						for (String variant : algorithm.getVariants()) {  // loop variants
	
							AlgorithmArguments arguments = new AlgorithmArguments(algotithmName, variant, algorithm.getInfo(), functionNumber, individualSize);
							populationSize = arguments.getPopulationSize();
	
							List<Double> listOfMaxFES = new ArrayList<Double>();
							List<Double> listOfBests = new ArrayList<Double>();
							List<Double> listOfMeans = new ArrayList<Double>();
							//List<Double> listOfMedians = new ArrayList<Double>();
	
							String fileRoundErrorsName = PATH_RESULT + '\\' + algotithmName + '\\' + arguments.getPrefixFile() + "_evolution" + sufix + ".csv";
							int bestRoundColumn = bestRound(fileRoundErrorsName);
							extracted(listOfMaxFES, listOfBests, listOfMeans, fileRoundErrorsName, bestRoundColumn);

							String fileRoundFeDiffName = PATH_RESULT + '\\' + algotithmName + '\\' + arguments.getPrefixFile() + "_evolution_diff.csv";
							List<Double> listOfBestsFeDiff = new ArrayList<Double>();
							List<Double> listOfMeansFeDiff = new ArrayList<Double>();
							extracted(null, listOfBestsFeDiff, listOfMeansFeDiff, fileRoundFeDiffName, bestRoundColumn);

							String fileRoundMaxDistName = PATH_RESULT + '\\' + algotithmName + '\\' + arguments.getPrefixFile() + "_evolution_maxdist.csv";
							List<Double> listOfBestsMaxDist = new ArrayList<Double>();
							List<Double> listOfMeansMaxDist = new ArrayList<Double>();
							extracted(null, listOfBestsMaxDist, listOfMeansMaxDist, fileRoundMaxDistName, bestRoundColumn);
							
							String subTitle = String.format("%s - Função %d - Dimensão %d - NP %d", arguments.getTitleChart(), functionNumber, individualSize, populationSize);
							System.err.println(subTitle);
							// BEST
							if (bestEvolution == null) {
								bestEvolution = new EvolutionChart2D("Evolução dos Erros (L)", "Evolução dos Parâmetros (R)");
								String titleBest = String.format("Evolução dos Erros (L) x Parâmetros (R)");
								bestEvolution.setTitle(titleBest, subTitle);
							}
							bestEvolution.addSerie(listOfMaxFES, listOfBests, "Best (L)", 0);
							bestEvolution.addSerie(listOfMaxFES, listOfMeans, "Mean (L)", 0);
							bestEvolution.addSerie(listOfMaxFES, listOfMeansFeDiff, "Fmax - Fmin (R)", 1);
							bestEvolution.addSerie(listOfMaxFES, listOfMeansMaxDist, "Máx. Dist (R)", 1);
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
					File directoryBest = new File(directory.getAbsolutePath() + "\\best");
					if (!directoryBest.exists())
						directoryBest.mkdirs();
					String bestFilePng = directoryBest.getAbsolutePath() + String.format("\\P%d_F%d_D%d_best_evolution" + sufix + ".png", populationSize, functionNumber, individualSize);
					bestEvolution.toFile(bestFilePng);
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
			if (bestEvolution != null)
				bestEvolution.close(); // meanEvolution.close();// medianEvolution.close();
		}
	}
}