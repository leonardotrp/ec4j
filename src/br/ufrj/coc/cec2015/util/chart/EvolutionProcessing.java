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
		String PATH_RESULT = "D:\\Google Drive (COC)\\trabalho de dissertação\\2 - dpade with eig\\experimentos";

		for (int individualSize : Properties.INDIVIDUAL_SIZES) { // loop dimensions
			PATH_RESULT = PATH_RESULT + '\\' + "P100_D" + individualSize + '\\';

	        // Write the output to a file
			File directory = new File(PATH_RESULT + "\\D" + individualSize + '\\');
			if (!directory.exists())
				directory.mkdirs();
			
			for (int functionNumber : Properties.FUNCTION_NUMBERS) { // loop functions
				
				EvolutionChart2D meanEvolution = null;//, medianEvolution = null;

				for (String algotithmName : Properties.ALGORITHMS) { // loop algorithms
	
					String className = Algorithm.class.getPackage().getName() + '.' + algotithmName.toLowerCase() + '.' + algotithmName;
					Algorithm algorithm = Main.newInstanceAlgorithm(className);
	
					for (String variant : algorithm.getVariants()) {  // loop variants
					
						AlgorithmArguments arguments = new AlgorithmArguments(algotithmName, variant, algorithm.getInfo(), functionNumber, individualSize);

						List<Double> listOfMaxFES = new ArrayList<Double>();
						List<Double> listOfMeans = new ArrayList<Double>();
						List<Double> listOfMedians = new ArrayList<Double>();

						String fileRoundErrorsName = PATH_RESULT + '\\' + algotithmName + '\\' + arguments.getPrefixFile() + "_evolution.csv";
						BufferedReader br = null;
						try {
							br = new BufferedReader(new FileReader(fileRoundErrorsName));
							String line;
			                double maxFES = 0.001;
							while ((line = br.readLine()) != null && maxFES < 0.1) {

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
				                			System.err.println(columns.next());
				                		roundValues.add(roundValue);
				                	}
					                columns.close();

					                if (hasValue) {
					                	System.err.println(maxFES);
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
						
						String subTitle = String.format("Função %d - Dimensão %d", functionNumber, individualSize);
						if (meanEvolution == null) {
							meanEvolution = new EvolutionChart2D();
							String titleMean = String.format("Evolução da Média dos Erros (%d rodadas)", Properties.MAX_RUNS);
							meanEvolution.setTitle(titleMean, subTitle);
						}
						meanEvolution.addSerie(listOfMaxFES, listOfMeans, arguments.getTitleChart());
					}
				}
				
				String meanFilePng = directory.getAbsolutePath() + String.format("\\P100_F%d_D%d_mean_evolution.png", functionNumber, individualSize);
				meanEvolution.toFile(meanFilePng);
			}
			
		}
	}
}