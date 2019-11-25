package br.ufrj.coc.cec2015.util.chart;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import br.ufrj.coc.cec2015.Main;
import br.ufrj.coc.cec2015.algorithm.Algorithm;
import br.ufrj.coc.cec2015.algorithm.AlgorithmArguments;
import br.ufrj.coc.cec2015.util.Helper;
import br.ufrj.coc.cec2015.util.Properties;

public class EvolutionProcessing {

	public static void main(String[] args) throws Exception {
		String PATH_RESULT = "D:\\Google Drive (COC)\\trabalho de dissertação\\2 - jade with eig\\experimentos\\CR_JADE_EIG";
		String sufix = "";//"_maxdist";//"_diff";
		String label = "Erros";//"Máxima Distância";//"(Fmax - Fmin)";

		for (int individualSize : Properties.INDIVIDUAL_SIZES) { // loop dimensions

	        // Write the output to a file
			File directory = new File(PATH_RESULT + "\\gráficos\\D" + individualSize);
			if (!directory.exists())
				directory.mkdirs();
	
			for (int functionNumber : Properties.FUNCTION_NUMBERS) { // loop functions

				EvolutionChart2D meanEvolution = null, medianEvolution = null, stdEvolution = null;
				int populationSize = 0;
				for (String algotithmName : Properties.ALGORITHMS) { // loop algorithms
	
					String className = Algorithm.class.getPackage().getName() + '.' + algotithmName.toLowerCase() + '.' + algotithmName;
					Algorithm algorithm = Main.newInstanceAlgorithm(className);
	
					for (String variant : algorithm.getVariants()) {  // loop variants
						
						System.err.println(algorithm + " - " + variant);
					
						AlgorithmArguments arguments = new AlgorithmArguments(algotithmName, variant, algorithm.getInfo(), functionNumber, individualSize);
						populationSize = arguments.getPopulationSize();

						List<Double> listOfMaxFES = new ArrayList<Double>();
						List<Double> listOfMeans = new ArrayList<Double>();
						List<Double> listOfMedians = new ArrayList<Double>();
						List<Double> listOfStds = new ArrayList<Double>();

						String fileRoundErrorsName = PATH_RESULT + '\\' + algotithmName + '\\' + arguments.getPrefixFile() + "_evolution" + sufix + ".csv";
						System.err.println(fileRoundErrorsName);
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
	
				                	int round = 1;
				                	boolean hasValue = false;
				                	while (round++ <= Properties.MAX_RUNS) {
				                		Double roundValue = Properties.MIN_ERROR_VALUE;
				                		if (columns.hasNextDouble()) {
				                			hasValue = true;
				                			roundValue = columns.nextDouble();
				                		}
				                		else
				                			columns.next();
				                		roundValues.add(roundValue);
				                	}
					                columns.close();

					                if (hasValue) {
					                	//System.err.println(maxFES);
				                		listOfMaxFES.add(maxFES);
						                
						                double mean = Helper.calculateMean(roundValues);
						                listOfMeans.add(mean);
						                
						                double median = Helper.calculateMedian(roundValues);
						                listOfMedians.add(median);
						                
						                double std = Helper.calculateStandardDeviation(roundValues, mean);
						                listOfStds.add(std);
					                }
				                }
				            }
						} catch (FileNotFoundException e) {
							System.err.println("File not found: " + fileRoundErrorsName);
						} finally {
							if (br != null)
								br.close();
						}

						String subTitle = String.format("Função %d - Dimensão %d - NP %d", functionNumber, individualSize, populationSize);
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

						// STD
						if (stdEvolution == null) {
							stdEvolution = new EvolutionChart2D("Desvio Padrão dos " + label);
							String titleStd = String.format("Evolução do Desvio Padrão dos " + label + " (%d rodadas)", Properties.MAX_RUNS);
							stdEvolution.setTitle(titleStd, subTitle);
						}
						stdEvolution.addSerie(listOfMaxFES, listOfStds, arguments.getTitleChart());
					}
				}

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

				File directoryStd = new File(directory.getAbsolutePath() + "\\std");
				if (!directoryStd.exists())
					directoryStd.mkdirs();
				String stdFilePng = directoryStd.getAbsolutePath() + String.format("\\P%d_F%d_D%d_median_evolution" + sufix + ".png", populationSize, functionNumber, individualSize);
				stdEvolution.toFile(stdFilePng);
			}
		}
	}
}