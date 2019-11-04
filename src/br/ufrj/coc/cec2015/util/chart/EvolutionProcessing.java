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
import br.ufrj.coc.cec2015.util.Properties;
import br.ufrj.coc.cec2015.util.Statistic;

public class EvolutionProcessing {

	public static void main(String[] args) throws Exception {
		//String ROOT_PATH = "D:\\Google Drive (COC)\\trabalho de dissertação\\2 - jade with eig\\experimentos";
		String ROOT_PATH = "C:\\dev\\workspace\\CEC2015\\results";

		for (int individualSize : Properties.INDIVIDUAL_SIZES) { // loop dimensions
			String PATH_RESULT = ROOT_PATH + "\\P40-1280\\";// + "P40_D" + individualSize + '\\';

	        // Write the output to a file
			File directory = new File(PATH_RESULT + "\\chart\\");
			if (!directory.exists())
				directory.mkdirs();
	
			for (int functionNumber : Properties.FUNCTION_NUMBERS) { // loop functions

				EvolutionChart2D meanEvolution = null, medianEvolution = null;
				
				for (String algotithmName : Properties.ALGORITHMS) { // loop algorithms
	
					String className = Algorithm.class.getPackage().getName() + '.' + algotithmName.toLowerCase() + '.' + algotithmName;
					Algorithm algorithm = Main.newInstanceAlgorithm(className);
	
					for (String variant : algorithm.getVariants()) {  // loop variants
						
						System.err.println(algorithm + " - " + variant);
					
						AlgorithmArguments arguments = new AlgorithmArguments(algotithmName, variant, algorithm.getInfo(), functionNumber, individualSize);

						List<Double> listOfMaxFES = new ArrayList<Double>();
						List<Double> listOfMeans = new ArrayList<Double>();
						List<Double> listOfMedians = new ArrayList<Double>();

						String fileRoundErrorsName = PATH_RESULT + '\\' + algotithmName + '\\' + arguments.getPrefixFile() + "_evolution.csv";
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
						                
						                double mean = Statistic.calculateMean(roundValues);
						                listOfMeans.add(mean);
						                
						                double median = Statistic.calculateMedian(roundValues);
						                listOfMedians.add(median);
					                }
				                }
				            }
						} catch (FileNotFoundException e) {
							System.err.println("File not found: " + fileRoundErrorsName);
						} finally {
							if (br != null)
								br.close();
						}

						// MEAN
						String subTitleMean = String.format("Função %d - Dimensão %d", functionNumber, individualSize);
						if (meanEvolution == null) {
							meanEvolution = new EvolutionChart2D("Média dos Erros");
							String titleMean = String.format("Evolução da Média dos Erros (%d rodadas)", Properties.MAX_RUNS);
							meanEvolution.setTitle(titleMean, subTitleMean);
						}
						meanEvolution.addSerie(listOfMaxFES, listOfMeans, arguments.getTitleChart());

						// MEDIAN
						String subTitleMedian = String.format("Função %d - Dimensão %d", functionNumber, individualSize);
						if (medianEvolution == null) {
							medianEvolution = new EvolutionChart2D("Mediana dos Erros");
							String titleMedian = String.format("Evolução da Mediana dos Erros (%d rodadas)", Properties.MAX_RUNS);
							medianEvolution.setTitle(titleMedian, subTitleMedian);
						}
						medianEvolution.addSerie(listOfMaxFES, listOfMedians, arguments.getTitleChart());
					}
				}

				String meanFilePng = directory.getAbsolutePath() + String.format("\\P40_F%d_D%d_mean_evolution.png", functionNumber, individualSize);
				meanEvolution.toFile(meanFilePng);
				
				String medianFilePng = directory.getAbsolutePath() + String.format("\\P40_F%d_D%d_median_evolution.png", functionNumber, individualSize);
				medianEvolution.toFile(medianFilePng);
			}
		}
	}
}